package com.monk.lottery.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;

import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.dubbo.common.json.JSONObject;

@SuppressWarnings("deprecation")
public class HttpPooledInvoker {

//	private static class Holder {
//		private static HttpInvoker instance = new HttpInvoker();
//	}
	private static final Logger log = LoggerFactory.getLogger(HttpPooledInvoker.class);

	private CloseableHttpClient client;
	private String host;
	//private ResponseHandler<String> handler;
	private HttpResponseHandler httpResponseHandler = new HttpResponseHandler();
	PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
	private boolean wrap;
	
	//private RequestConfig requestConfig;
	
	public HttpPooledInvoker() {
		build(null);
	}

//	public static HttpInvoker getInstance() {
//		return Holder.instance;
//	}
	
	public void build(String host) {
		this.host = host;
		if (this.host != null && this.host.trim().toLowerCase().startsWith("https")) {
			try {
				SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null,
						new TrustStrategy() {
							// 信任所有
							public boolean isTrusted(X509Certificate[] chain, String authType)
									throws CertificateException {
								return true;
							}
						}).build();
				SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext);
				client = HttpClients.custom().setConnectionManager(cm).setSSLSocketFactory(sslsf)
						.build();
			} catch (KeyManagementException e) {
				log.error("KeyManagementException in HttpPooledInvoker.build()", e);
			} catch (NoSuchAlgorithmException e) {
				log.error("NoSuchAlgorithmException in HttpPooledInvoker.build()", e);
			} catch (KeyStoreException e) {
				log.error("KeyStoreException in HttpPooledInvoker.build()", e);
			}
		} else {
			client = HttpClients.custom().setConnectionManager(cm).build();
		}
	}

	public HttpPooledInvoker setHost(String host) {
		this.build(host);
		return this;
	}

	public HttpPooledInvoker setWrap(boolean wrap) {
		this.wrap = wrap;
		return this;
	}

	public HttpPooledInvoker setSSL() {
		return this;
	}
	
	public HttpPooledInvoker setSSL(String keyPath,String keystorePass) {
		try {

			KeyStore trustStore = KeyStore.getInstance(KeyStore
					.getDefaultType());
			FileInputStream instream = new FileInputStream(new File(keyPath));
			// 密匙库的密码
			trustStore.load(instream, keystorePass.toCharArray());

			SSLSocketFactory ssf = new SSLSocketFactory(trustStore);
			ssf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
			Scheme sch = new Scheme("https", ssf, 443);
			client.getConnectionManager().getSchemeRegistry().register(sch);
		} catch (KeyManagementException e) {
			log.error(e.getMessage(), e);
		} catch (NoSuchAlgorithmException e) {
			log.error(e.getMessage(), e);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return this;
	}

	private String makeURL(String action) {
		if (action == null) {
			action = "";
		}
//		if (action.startsWith("/")) {
//			action = action.substring(1);
//		}
		return (this.host == null ? "" : this.host) + action;
	}

	private void agent(HttpRequestBase request) {
		request.setHeader(
				"Accept",
				"text/html,application/xhtml+xml,application/xml,image/png,image/*;q=0.8,*/*;q=0.5");
		request.setHeader("Accept-Language", "zh-cn,zh;q=0.5");
		request.setHeader("User-Agent",
				"Mozilla/5.0 (Windows NT 6.1; rv:8.0) Gecko/20100101 Firefox/8.0");
		request.setHeader("Accept-Encoding", "gzip, deflate");
		request.setHeader("Accept-Charset", "GB2312,utf-8;q=0.7,*;q=0.7");
		request.setHeader("Connection", "Keep-Alive");
	}

	public String get(String action) {
		CloseableHttpResponse response = getHttpResponse(action);
		if (response == null) {
			return null;
		}
		try {
			return httpResponseHandler.handleResponse(response);
		} catch (HttpResponseException e) {
			e.printStackTrace();
			log.error(e.getMessage(), e);
		} catch (IOException e) {
			e.printStackTrace();
			log.error(e.getMessage(), e);
		}
		return null;
	}
	

	public String delete(String action, Map<String, String> headers){
		CloseableHttpResponse response = doDelete(action, headers);
		if (response == null) {
			return null;
		}
		try {
			return httpResponseHandler.handleResponse(response);
		} catch (HttpResponseException e) {
			e.printStackTrace();
			log.error(e.getMessage(), e);
		} catch (IOException e) {
			e.printStackTrace();
			log.error(e.getMessage(), e);
		}
		return null;
		
	}
		
	public CloseableHttpResponse doDelete(String action,Map<String, String> headers) {
		HttpDelete delete = new HttpDelete(makeURL(action));
		if (wrap) {
			agent(delete);
		}
		try {
			if (headers != null && !headers.isEmpty()) {
				for (Map.Entry<String, String> entry : headers.entrySet()) {
					delete.addHeader(entry.getKey(),entry.getValue());
				}
			}
			return client.execute(delete,HttpClientContext.create());
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			log.error(e.getMessage(), e);
		} catch (IOException e) {
			e.printStackTrace();
			log.error(e.getMessage(), e);
		}
		return null;
	}
	
	
	public String delete(String action,Map<String, String> params, Map<String, String> headers){
		CloseableHttpResponse response = doDelete(action,params, headers);
		if (response == null) {
			return null;
		}
		try {
			return httpResponseHandler.handleResponse(response);
		} catch (HttpResponseException e) {
			e.printStackTrace();
			log.error(e.getMessage(), e);
		} catch (IOException e) {
			e.printStackTrace();
			log.error(e.getMessage(), e);
		}
		return null;
		
	}
	public CloseableHttpResponse doDelete(String action,Map<String, String> params, Map<String, String> headers) {
		HttpDelete delete = new HttpDelete(makeURL(action));
		if (wrap) {
			agent(delete);
		}
		try {
			if(params != null && params.size() > 0){
				HttpParams hp = new BasicHttpParams();
				for (Map.Entry<String, String> entry : headers.entrySet()) {
					hp.setParameter(entry.getKey(), entry.getValue());
				}
				delete.setParams(hp);
			}
			
			if (headers != null && !headers.isEmpty()) {
				for (Map.Entry<String, String> entry : headers.entrySet()) {
					delete.addHeader(entry.getKey(),entry.getValue());
				}
			}
			return client.execute(delete,HttpClientContext.create());
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			log.error(e.getMessage(), e);
		} catch (IOException e) {
			e.printStackTrace();
			log.error(e.getMessage(), e);
		}
		return null;
	}
	
	public String get(String action, Map<String, String> headers) {
		CloseableHttpResponse response = doGet(action, headers);
		if (response == null) {
			return null;
		}
		try {
			return httpResponseHandler.handleResponse(response);
		} catch (HttpResponseException e) {
			e.printStackTrace();
			log.error(e.getMessage(), e);
		} catch (IOException e) {
			e.printStackTrace();
			log.error(e.getMessage(), e);
		}
		return null;
	}
	
	public String head(String action, Map<String, String> headers) {
		CloseableHttpResponse response = doHead(action, headers);
		if (response == null) {
			return null;
		}
		try {
			HeaderIterator i = response.headerIterator();
			JSONObject json = new JSONObject();
			while(i.hasNext()){
				Header h = i.nextHeader();
				json.put(h.getName(), h.getValue());
			}
			return json.toString();
		}catch (Exception e) {
			e.printStackTrace();
			log.error(e.getMessage(), e);
		}
		return null;
	}
	public CloseableHttpResponse doHead(String action,Map<String, String> headers) {
		HttpHead head = new HttpHead(makeURL(action));
		if (wrap) {
			agent(head);
		}
		try {
			if (headers != null && !headers.isEmpty()) {
				for (Map.Entry<String, String> entry : headers.entrySet()) {
					head.addHeader(entry.getKey(),entry.getValue());
				}
			}
			return client.execute(head,HttpClientContext.create());
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			log.error(e.getMessage(), e);
		} catch (IOException e) {
			e.printStackTrace();
			log.error(e.getMessage(), e);
		}
		return null;
	}
	
	public CloseableHttpResponse getHttpResponse(String action) {
		return doGet(action);
	}
	public String get(String action, Map<String, String> params, Map<String, String> headers) {
		StringBuffer sBuff = new StringBuffer();
		sBuff.append(action);
		String temp = "";
		if(StringUtils.isNotBlank(action)){
			temp = action;
		}else if(StringUtils.isNotBlank(host)){
			temp = this.host;
		}else{
			throw new RuntimeException("invalid host");
		}
		if(!params.isEmpty()){
			if(temp.indexOf("?") != -1){
				sBuff.append("&");
			}else{
				sBuff.append("?");
			}
		}
		int i = 0;
		for (Map.Entry<String, String> entry : params.entrySet()) {
			String value = entry.getValue();
			if (value != null) {
				if(i++ != 0){
					sBuff.append("&");
				}
				sBuff.append(entry.getKey());
				sBuff.append("=");
				sBuff.append(value);
			}
		}
		return get(sBuff.toString(),headers);
	}
	public HttpResponse getHttpResponse(String action, Map<String, String> params) {
		StringBuffer sBuff = new StringBuffer();
		sBuff.append(action);
		String temp = "";
		if(StringUtils.isNotBlank(action)){
			temp = action;
		}else if(StringUtils.isNotBlank(host)){
			temp = this.host;
		}else{
			throw new RuntimeException("invalid host");
		}
		if(!params.isEmpty()){
			if(temp.indexOf("?") != -1){
				sBuff.append("&");
			}else{
				sBuff.append("?");
			}
		}
		int i = 0;
		for (Map.Entry<String, String> entry : params.entrySet()) {
			String value = entry.getValue();
			if (value != null) {
				if(i++ != 0){
					sBuff.append("&");
				}
				sBuff.append(entry.getKey());
				sBuff.append("=");
				sBuff.append(value);
			}
		}
		return getHttpResponse(sBuff.toString());
	}

	public CloseableHttpResponse doGet(String action) {
		HttpGet get = new HttpGet(makeURL(action));
		if (wrap) {
			agent(get);
		}
		try {
			return client.execute(get,HttpClientContext.create());
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			log.error(e.getMessage(), e);
		} catch (IOException e) {
			e.printStackTrace();
			log.error(e.getMessage(), e);
		}
		return null;
	}
	
	
	public String get(String action,String data, Map<String, String> headers) {
		StringBuffer sBuff = new StringBuffer();
		sBuff.append(action);
		
		String temp = "";
		if(StringUtils.isNotBlank(action)){
			temp = action;
		}else if(StringUtils.isNotBlank(host)){
			temp = this.host;
		}else{
			throw new RuntimeException("invalid host");
		}
		
		if(temp.indexOf("?") != -1){
			sBuff.append("&");
		}else{
			sBuff.append("?");
		}
		sBuff.append(data);
		return get(sBuff.toString(),headers);
	}

	public CloseableHttpResponse doGet(String action, String data,Map<String, String> headers) {
		HttpGet get = new HttpGet(makeURL(action));
		if (wrap) {
			agent(get);
		}
		try {
			if (headers != null && !headers.isEmpty()) {
				for (Map.Entry<String, String> entry : headers.entrySet()) {
					get.addHeader(entry.getKey(),entry.getValue());
				}
			}
			return client.execute(get,HttpClientContext.create());
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			log.error(e.getMessage(), e);
		} catch (IOException e) {
			e.printStackTrace();
			log.error(e.getMessage(), e);
		}
		return null;
	}
	public CloseableHttpResponse doGet(String action, Map<String, String> headers) {
		HttpGet get = new HttpGet(makeURL(action));
		//get.setConfig(requestConfig);
		
		if (wrap) {
			agent(get);
		}
		
		try {
			if (headers != null && !headers.isEmpty()) {
				for (Map.Entry<String, String> entry : headers.entrySet()) {
					get.addHeader(entry.getKey(),entry.getValue());
				}
			}
			return client.execute(get,HttpClientContext.create());
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			log.error(e.getMessage(), e);
		} catch (IOException e) {
			e.printStackTrace();
			log.error(e.getMessage(), e);
		}
		return null;
	}

	public String post(String action, Map<String, String> params) {
		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		if (params != null && !params.isEmpty()) {
			for (Map.Entry<String, String> entry : params.entrySet()) {
				String value = entry.getValue();
				if (value != null) {
					NameValuePair nvp = new BasicNameValuePair(entry.getKey(), value);
					pairs.add(nvp);
				}
			}
		}
		try {
			return doPost(action, new UrlEncodedFormEntity(pairs,HTTP.UTF_8));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			log.error(e.getMessage(), e);
		}
		return null;
	}
	
	public String post(String action, Map<String, String> params,Map<String, String> headers) {
		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		
		if (params != null && !params.isEmpty()) {
			for (Map.Entry<String, String> entry : params.entrySet()) {
				String value = entry.getValue();
				if (value != null) {
					NameValuePair nvp = new BasicNameValuePair(entry.getKey(), value);
					pairs.add(nvp);
				}
			}
		}
		try {
			return doPost(action, new UrlEncodedFormEntity(pairs,HTTP.UTF_8),headers);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			log.error(e.getMessage(), e);
		}
		return null;
	}
	
	
	
	public String postStringBody(String action, String body) {
		try {
			 StringEntity reqEntity = new StringEntity(body,"utf-8");
			 reqEntity.setContentType("application/json");
			 return doPost(action, reqEntity);
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e.getMessage(), e);
		}
		return null;
	}
	
	public String postStringBody(String action, String body,Map<String, String> headers) {
		try {
			 StringEntity reqEntity = new StringEntity(body,"utf-8");
			 reqEntity.setContentType("application/json");
			 return doPost(action, reqEntity,headers);
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e.getMessage(), e);
		}
		return null;
	}
	private String doPost(String action, HttpEntity entity) {
		HttpPost post = new HttpPost(makeURL(action));
		//post.setConfig(requestConfig);
		if (wrap) {
			agent(post);
		}
		try {
			post.setEntity(entity);
			CloseableHttpResponse response = client.execute(post,HttpClientContext.create());
			return this.httpResponseHandler.handleResponse(response);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			log.error(e.getMessage(), e);
			throw new RuntimeException(e);
		} catch (IOException e) {
			e.printStackTrace();
			log.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}
	
	private String doPost(String action, HttpEntity entity,Map<String, String> headers) {
		HttpPost post = new HttpPost(makeURL(action));
		//post.setConfig(requestConfig);
		if (wrap) {
			agent(post);
		}
		try {
			post.setEntity(entity);
			if (headers != null && !headers.isEmpty()) {
				for (Map.Entry<String, String> entry : headers.entrySet()) {
					post.addHeader(entry.getKey(),entry.getValue());
				}
			}
			CloseableHttpResponse response = client.execute(post,HttpClientContext.create());
			return this.httpResponseHandler.handleResponse(response);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			log.error(e.getMessage(), e);
			throw new RuntimeException(e);
		} catch (IOException e) {
			e.printStackTrace();
			log.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}


	public void destroy() {
		if (client != null) {
			client.getConnectionManager().shutdown();
		}
	}
	public String getCookie(String key){
		if(null == client || null == key){
			return null;
		}
		CookieStore cookieStore = ((DefaultHttpClient) client).getCookieStore();
		if(null == cookieStore){
			return null;
		}
		List<Cookie> cookies = cookieStore.getCookies();
		if(null == cookies || cookies.size() < 1){
			return null;
		}
		for(Cookie c:cookies){
			String name = c.getName();
			if(null != name && name.equals(key) ){
				return c.getValue();
			}
		}
		return null;
	}
	
	public String upload(String action, InputStream in) {
		try {
			InputStreamEntity reqEntity = new InputStreamEntity(in);
			 reqEntity.setContentType("application/xml");
			 return doPost(action, reqEntity, null);
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e.getMessage(), e);
		}
		return null;
	}
	
	public String upload(String action, byte[] in) {
		try {
			ByteArrayEntity reqEntity = new ByteArrayEntity(in);
			 reqEntity.setContentType("application/xml");
			 return doPost(action, reqEntity, null);
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e.getMessage(), e);
		}
		return null;
	}
	
	public static void main(String[] args) {
		HttpPooledInvoker invoker = new HttpPooledInvoker();
		invoker.setHost("https://safe.tclclouds.com/account/auth");
		String string = invoker.get("");
		System.out.println(string);
	}
	
	class HttpResponseHandler {  
		  
         public String handleResponse(CloseableHttpResponse response) throws ClientProtocolException, IOException {  
			try {
				try {
					HttpEntity entity = response.getEntity();
					if (entity != null) {
						String ret = EntityUtils.toString(entity,"utf-8");
						EntityUtils.consume(entity);
						return ret;
						/*
						 * if(response.getStatusLine().getStatusCode() ==
						 * HttpStatus.SC_OK) { return new
						 * String(EntityUtils.toByteArray(entity),"utf-8"); }
						 */
					}
				} finally {
					response.close();
				}
			} catch (ClientProtocolException ex) {
				// Handle protocol errors
			} catch (IOException ex) {
				// Handle I/O errors
			}
            
             return "{}";  
         }  
	}

}