package com.ifair.common.security;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.security.Key;

public class DESCoder {
	/**
	 * 密钥算法 java支持56位密钥，bouncycastle支持64位
	 */
	public static final String KEY_ALGORITHM = "DES";

	/**
	 * 加密/解密算法/工作模式/填充方式
	 */
	public static final String CIPHER_ALGORITHM = "DES/ECB/PKCS5Padding";

	/**
	 *
	 * 生成密钥，java6只支持56位密钥，bouncycastle支持64位密钥
	 * 
	 * @return byte[] 二进制密钥
	 */
	public static byte[] initkey() throws Exception {

		// 实例化密钥生成器
		KeyGenerator kg = KeyGenerator.getInstance(KEY_ALGORITHM);
		// 初始化密钥生成器
		kg.init(56);
		// 生成密钥
		SecretKey secretKey = kg.generateKey();
		// 获取二进制密钥编码形式
		return secretKey.getEncoded();
	}

	/**
	 * 转换密钥
	 * 
	 * @param key
	 *            二进制密钥
	 * @return Key 密钥
	 */
	public static Key toKey(byte[] key) throws Exception {
		// 实例化Des密钥
		DESKeySpec dks = new DESKeySpec(key);
		// 实例化密钥工厂
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(KEY_ALGORITHM);
		// 生成密钥
		SecretKey secretKey = keyFactory.generateSecret(dks);
		return secretKey;
	}

	/**
	 * 加密数据
	 * 
	 * @param data
	 *            待加密数据
	 * @param key
	 *            密钥
	 * @return byte[] 加密后的数据
	 */
	public static byte[] encrypt(byte[] data, byte[] key) throws Exception {
		// 还原密钥
		Key k = toKey(key);
		// 实例化
		Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
		// 初始化，设置为加密模式
		cipher.init(Cipher.ENCRYPT_MODE, k);
		// 执行操作
		return cipher.doFinal(data);
	}

	public static String encrypt(String data, String key) throws Exception {
		return Base64.encodeBase64String(encrypt(data.getBytes("utf8"), Base64.decodeBase64(key)));
	}

	/**
	 * 解密数据
	 * 
	 * @param data
	 *            待解密数据
	 * @param key
	 *            密钥
	 * @return byte[] 解密后的数据
	 */
	public static byte[] decrypt(byte[] data, byte[] key) throws Exception {
		// 欢迎密钥
		Key k = toKey(key);
		// 实例化
		Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
		// 初始化，设置为解密模式
		cipher.init(Cipher.DECRYPT_MODE, k);
		// 执行操作
		return cipher.doFinal(data);
	}

	public static String decrypt(String data, String key) throws Exception {
		return new String(decrypt(Base64.decodeBase64(data), Base64.decodeBase64(key.getBytes("utf8"))), "utf8");
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		String str = "{\"createdAt\":\"2016-07-28 14:42:16\",\"discountPrice\":20,\"memo\":\"要大 要更大\",\"orderList\":[{\"bn\":\"8801105910964\",\"discountPrice\":5,\"itemId\":1234,\"itemPrice\":5,\"num\":4,\"oid\":6000009090014427,\"price\":15,\"skuId\":2345,\"title\":\"海太水蜜桃果肉饮料238ml\"},{\"bn\":\"6924593277745\",\"discountPrice\":15,\"itemId\":31245,\"itemPrice\":10,\"num\":10,\"oid\":6000009030014378,\"price\":185,\"skuId\":8765892,\"title\":\"70g傻小子五香卤汁豆干\"}],\"payType\":\"offline\",\"postPrice\":0,\"price\":200,\"shopInfo\":{\"name\":\"测试供应商\",\"shopId\":201345},\"tid\":6000009060025830,\"tradeStatus\":\"WAIT_SELLER_SEND_GOODS\",\"userInfo\":{\"name\":\"测试用户\",\"receiverAddress\":\"联胜路10号创客云立方1号楼202\",\"receiverCity\":\"杭州\",\"receiverDistrict\":\"余杭\",\"receiverGps\":\"120.027402,30.239504\",\"receiverMobile\":\"13000000001\",\"receiverName\":\"测试收件人姓名\",\"receiverState\":\"浙江省\",\"userId\":212345}}";
		System.out.println("原文：" + str);
		// 初始化密钥
		byte[] key = DESCoder.initkey();
		System.out.println("密钥：" + Base64.encodeBase64String(key));
		// 加密数据
		byte[] data = DESCoder.encrypt(str.getBytes(), key);
		System.out.println("加密后：" + Base64.encodeBase64String(data));
		// 解密数据
		data = DESCoder.decrypt(data, key);
		System.out.println("解密后：" + new String(data));

		String bk = "Dub91Teh9No=";
		String encrypt = encrypt(str, bk);
		System.out.println(encrypt);
		System.out.println(decrypt(encrypt, bk));
	}
}
