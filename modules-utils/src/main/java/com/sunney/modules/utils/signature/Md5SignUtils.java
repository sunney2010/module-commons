package com.sunney.modules.utils.signature; 

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.sunney.modules.utils.encrypt.Md5Util;

import net.sf.json.JSONObject;


/**
 * Description:
 * ͨ��MD5����Կ���ַ������м���
 * @author  LiChunming
 * @version V1.0 
 * @createDateTime��2013-1-11 ����11:25:59 
 * @Company: MSD. 
 * @Copyright: Copyright (c) 2011
 **/
public class Md5SignUtils {
	/** 
     * ���ܣ�����ǩ�����
     * @param sArray Ҫ���ܵ�����
     * @param key ��ȫУ����
     * @return ǩ������ַ���
     */
    public static String buildObjectSign(Object object, String key) {
    	//�Ѷ���תΪHash
    	HashMap<String, Object> sArray= ObjecttoHashMap(object);
    	String mysign=buildMashSign(sArray,key);
        return mysign;
    }
	/** 
     * ���ܣ�����ǩ�����
     * @param sArray Ҫ���ܵ�����
     * @param key ��ȫУ����
     * @return ǩ������ַ���
     */
    public static String buildMashSign(Map<String,Object> sArray, String key) {
    	//��key��������
    	sArray.put("key", key);
    	//����������Ԫ�أ����ա�����=����ֵ����ģʽ�á�&���ַ�ƴ�ӳ��ַ���
        String prestr = createLinkString(sArray);  
        String mysign = Md5Util.md5(prestr).toUpperCase();
        return mysign;
    }
    /**
	 * ���ַ�������MD5����
	 * @param prestr �����ܵ��ַ���
	 * @param aKey ��Կ
	 * @return
	 */
	public static String buildStringSign(String prestr, String aKey) {
		  String mysign = Md5Util.md5(prestr).toUpperCase();
	      return mysign;
	}
	/**
	 * ���ַ��������MD5����
	 * @param args �ַ�����
	 * @param key ��Կ
	 * @return
	 */
	public static String buildStringSign(String[] args, String key) {
		if (args == null || args.length == 0) {
			return (null);
		}
		StringBuffer str = new StringBuffer();
		for (int i = 0; i < args.length; i++) {
			str.append(args[i]);
		}
		return (buildStringSign(str.toString(), key));
	}
	/**
	 * ����תΪHashMap
	 * @param object
	 * @return
	 */
	 public static HashMap<String, Object> ObjecttoHashMap(Object object) {
		  HashMap<String, Object> data = new HashMap<String, Object>();
		  JSONObject jsonObject = JSONObject.fromObject(object);
		  Iterator<?> it = jsonObject.keys();
		  while (it.hasNext()) {
		   String key = String.valueOf(it.next());
		   Object value = jsonObject.get(key);
		   data.put(key, value);
		  }
		  return data;
	}
	 /** 
     * ���ܣ�����������Ԫ�����򣬲�����"����=����ֵ"��ģʽ��"&"�ַ�ƴ�ӳ��ַ���
     * @param params ��Ҫ���򲢲����ַ�ƴ�ӵĲ�����
     * @return ƴ�Ӻ��ַ���
     */
    public static String createLinkString(Map<String,Object> params){
        List<String> keys = new ArrayList<String>(params.keySet());
        //�Բ�����ĸ��������
        Collections.sort(keys);
        String prestr = "";
        for (int i = 0; i < keys.size(); i++) {
            String key = (String) keys.get(i);
            String value = params.get(key).toString();
            if (i == keys.size() - 1) {//ƴ��ʱ�����������һ��&�ַ�
                prestr = prestr + key + "=" + value;
            } else {
                prestr = prestr + key + "=" + value + "&";
            }
        }
        return prestr;
    }
    /** 
     * ���ܣ�����������Ԫ�ذ���"����=����ֵ"��ģʽ��"&"�ַ�ƴ�ӳ��ַ���
     * Ӧ�ó�����ʹ�ó�����GET��ʽ����ʱ����URL�����Ľ��б���
     * @param params ��Ҫ���򲢲����ַ�ƴ�ӵĲ�����
     * @param input_charset �����ʽ
     * @return ƴ�Ӻ��ַ���
     */
    public static String createLinkString_urlencode(Map<String,String> params, String input_charset){
        List<String> keys = new ArrayList<String>(params.keySet());
        //�Բ�����ĸ��������
        Collections.sort(keys);
        String prestr = "";
        for (int i = 0; i < keys.size(); i++) {
            String key = (String) keys.get(i);
            String value = (String) params.get(key);
            try {
                prestr = prestr + key + "=" + URLEncoder.encode(value,input_charset) + "&";
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return prestr;
    }
}
 