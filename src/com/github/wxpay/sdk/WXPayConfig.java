package com.github.wxpay.sdk;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;

public class WXPayConfig {

	private byte[] certData;
	private String AppID = "";
	private String MchID = "";
	private String Key = "";
	private String certPath = "";

	public final static String IP = "8.8.8.8";
	
	public WXPayConfig(HashMap<String, String> vparams) {
		AppID = vparams.get("AppID").toString();
		MchID = vparams.get("MchID").toString();
		Key = vparams.get("Key").toString();
		certPath = vparams.get("certPath").toString();
		getCert();
	}

	private void getCert() {
		try {
			File file = new File(certPath);
			InputStream certStream = new FileInputStream(file);
			this.certData = new byte[(int) file.length()];
			certStream.read(this.certData);
			certStream.close();
		} catch (Exception ex) {

		}
	}

	/**
	 * ��ȡ App ID
	 *
	 * @return App ID
	 */
	public String getAppID() {
		return AppID;
	}

	/**
	 * ��ȡ Mch ID
	 *
	 * @return Mch ID
	 */
	public String getMchID() {
		return MchID;
	}

	/**
	 * ��ȡ API ��Կ
	 *
	 * @return API��Կ
	 */
	public String getKey() {
		return Key;
	}

	/**
	 * ��ȡ�̻�֤������
	 *
	 * @return �̻�֤������
	 */
	public InputStream getCertStream() {
		ByteArrayInputStream certBis;
		certBis = new ByteArrayInputStream(this.certData);
		return certBis;
	}

	/**
	 * HTTP(S) ���ӳ�ʱʱ�䣬��λ����
	 *
	 * @return
	 */
	public int getHttpConnectTimeoutMs() {
		return 6 * 1000;
	}

	/**
	 * HTTP(S) �����ݳ�ʱʱ�䣬��λ����
	 *
	 * @return
	 */
	public int getHttpReadTimeoutMs() {
		return 8 * 1000;
	}

	/**
	 * ��ȡWXPayDomain, ���ڶ����������Զ��л�
	 * 
	 * @return
	 */
	public IWXPayDomain getWXPayDomain() {
		return WXPayDomainSimpleImpl.instance();
	};

	/**
	 * �Ƿ��Զ��ϱ��� ��Ҫ�ر��Զ��ϱ���������ʵ�ָú������� false ���ɡ�
	 *
	 * @return
	 */
	public boolean shouldAutoReport() {
		return true;
	}

	/**
	 * ���н����ϱ����̵߳�����
	 *
	 * @return
	 */
	public int getReportWorkerNum() {
		return 6;
	}

	/**
	 * �����ϱ�������Ϣ����������������߳�ȥ�����ϱ� ���Լ��㣺����һ����Ϣ200B��10000��Ϣռ�ÿռ� 2000 KB��ԼΪ2MB�����Խ���
	 *
	 * @return
	 */
	public int getReportQueueMaxSize() {
		return 10000;
	}

	/**
	 * �����ϱ���һ������ϱ��������
	 *
	 * @return
	 */
	public int getReportBatchSize() {
		return 10;
	}

}

