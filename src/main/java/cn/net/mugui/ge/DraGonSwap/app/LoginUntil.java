package cn.net.mugui.ge.DraGonSwap.app;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.xerial.snappy.Snappy;

import com.mugui.util.Other;


public class LoginUntil {

	public static byte[] ArrayBytesDecrypt(byte[] data) {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
		byte[] key = new byte[16];
		try {
			int i;
			for (i = 0; i < key.length; i++) {
				key[i] = (byte) (data[i] - 125);
			}
			for (; i < data.length; i++) {
				data[i] = (byte) (key[i % 16] ^ (data[i] + key[i % 16]));
				dataOutputStream.writeByte(data[i]);
			}
			data = outputStream.toByteArray();
			dataOutputStream.close();
			outputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally {
			try {
				dataOutputStream.close();
				outputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return data;
	}

	public static byte[] ArrayBytesEncryption(byte[] data) {
		byte[] temp = new byte[data.length];
		System.arraycopy(data, 0, temp, 0, temp.length);

		String uuid = Other.getShortUuid();
		byte[] string = uuid.getBytes();// 转化为字节数组
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();// 创建一个字节流
		DataOutputStream dataOutputStream = new DataOutputStream(outputStream);// 创建一个数据流，并装载字节流
		try {
			int len = string.length;
			// 遍历uuid的字节数组
			for (int i = 0; i < len; i++) {// 向新的数据流写入计算后的uuid
				dataOutputStream.writeByte(string[i] + 125);// 一个字节一个字节的写入字节流
			}
			for (int i = 0; i < temp.length; i++) {// 遍历需重编码数据并用uuid进行计算
				temp[i] = (byte) ((string[i % len] ^ temp[i]) - (string[i % len]));
				dataOutputStream.writeByte(temp[i]);
			}
			temp = outputStream.toByteArray();
			dataOutputStream.close();
			outputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return temp;// 返回生成的编码数据流
	}

	public static byte[] ZIPDecompressor(byte[] body, int yuan_len) {
		try {
			return Snappy.uncompress(body);
		} catch (IOException var3) {
			var3.printStackTrace();
			return null;
		}
	}
}
