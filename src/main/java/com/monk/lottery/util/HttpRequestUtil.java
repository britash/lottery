package com.monk.lottery.util;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

public class HttpRequestUtil {

    private static final Logger LOG = LoggerFactory.getLogger(HttpRequestUtil.class);

    private static CloseableHttpClient httpclient = HttpClients.createDefault();

    public static String get(String root, String uri) {
        String rsp = null;

        HttpGet httpGet = new HttpGet(root + uri);
        CloseableHttpResponse response = null;
        try {
            response = httpclient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            if (null != entity) {
                rsp = EntityUtils.toString(entity,"utf-8");
            }
        } catch (IOException e) {
            LOG.error(e.getMessage(), e.getCause());
        } finally {
            try {
                if (null != response) {
                    response.close();
                    response = null;
                }
            } catch (IOException e) {
                LOG.error(e.getMessage(), e.getCause());
            }
        }
        return rsp;
    }

    public static String post(String root, String uri, Object params) {
        String rsp = null;
        HttpPost httpPost = new HttpPost(root + uri);
        httpPost.setHeader("Content-Type", "application/json");
        CloseableHttpResponse response = null;
        try {
            StringEntity postEntity = new StringEntity(JSON.toJSONString(params),"utf-8");
            postEntity.setContentType("application/json");
            postEntity.setContentEncoding("UTF-8");
            httpPost.setEntity(postEntity);

            response = httpclient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            rsp = EntityUtils.toString(entity);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e.getCause());
        } finally {
            try {
                if (null != response) {
                    response.close();
                    response = null;
                }
            } catch (IOException e) {
                LOG.error(e.getMessage(), e.getCause());
            }
        }
        return rsp;
    }

}
