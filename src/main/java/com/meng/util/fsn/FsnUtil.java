package com.meng.util.fsn;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.ZipUtil;
import com.google.common.collect.Lists;
import com.meng.util.HelpMe;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author mengdexuan on 2018/8/6 11:42.
 */
public class FsnUtil {

	/**
	 * 提取fsn文件中的币值属性
	 *
	 * @param fsnInfo
	 * @return
	 */
	public static String valuta(FsnInfo fsnInfo) {
		if (fsnInfo != null) {
			return fsnInfo.getFsnRows().get(0).getValuta() + "";
		}
		return null;
	}

	/**
	 * 提取fsn文件(如:/usr/local/abc.fsn)中的币值属性
	 *
	 * @param fsnPath
	 * @return
	 */
	public static String valuta(String fsnPath) {
		return valuta(fsnInfo(fsnPath));
	}

	/**
	 * 提取fsn压缩包文件(如:/usr/local/abc.zip)中的币值属性
	 *
	 * @param fsnZipPath
	 * @return
	 */
	public static String valutaByFsnZip(String fsnZipPath) {
		return valuta(fsnInfoByFsnZipPath(fsnZipPath));
	}

	/**
	 * 读取fsn：通过fns文件(如：/usr/local/abc.fsn)的字节码，获取 FsnInfo
	 *
	 * @param fsnBytes
	 * @return
	 */
	public static FsnInfo fsnInfo(byte[] fsnBytes) {
		try {
			FsnReader fsnReader = new FsnReader("");
			FsnInfo fsnInfo = fsnReader.readFsn(fsnBytes);
			return fsnInfo;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}


	/**
	 * 通过fns文件路径(如：/usr/local/abc.fsn)，获取 FsnInfo
	 *
	 * @param fsnPath
	 * @return
	 */
	public static FsnInfo fsnInfo(String fsnPath) {
		FsnReader fsnReader = new FsnReader(fsnPath);
		try {
			return fsnReader.readFsn();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}


	/**
	 * 通过fns文件(如：/usr/local/abc.fsn)，获取 FsnInfo
	 *
	 * @param fsnFile
	 * @return
	 */
	public static FsnInfo fsnInfo(File fsnFile) {
		FsnReader fsnReader = new FsnReader(fsnFile.getPath());
		try {
			return fsnReader.readFsn();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}


	/**
	 * 通过 fsn 压缩包的路径（如：/usr/local/abc.zip），获取 FsnInfo
	 *
	 * @param fsnZipPath
	 * @return
	 */
	public static FsnInfo fsnInfoByFsnZipPath(String fsnZipPath) {
		return fsnInfoByFsnZipPath(FileUtil.file(fsnZipPath));
	}


	/**
	 * 通过 fsn 压缩包的路径（如：/usr/local/abc.zip），获取 FsnInfo
	 *
	 * @param fsnZip
	 * @return
	 */
	public static FsnInfo fsnInfoByFsnZipPath(File fsnZip) {
		File dir = ZipUtil.unzip(fsnZip);
		File fsnFile = FileUtil.loopFiles(dir).get(0);
		FsnInfo fsnInfo = fsnInfo(fsnFile);
		FileUtil.del(dir);
		return fsnInfo;
	}


	/**
	 * 提取gzh压缩包中的fsn文件到desc目录中，只要指定编码的fsn文件
	 *
	 * @param gzhPath
	 * @param desc
	 * @param fsnNameArr 指定编码的fsn文件（fsn文件是以[编码_时间.fsn]命名的）
	 */
	public static void extractFsnFromGzh(String gzhPath, String desc, String... fsnNameArr) {
		File tempFile = ZipUtil.unzip(gzhPath);

		File[] files = tempFile.listFiles();
		Arrays.stream(files).forEach(file -> {
			if (FileUtil.extName(file).equalsIgnoreCase("zip")) {
				File tempFile2 = ZipUtil.unzip(file);
				File[] files2 = tempFile2.listFiles();
				Arrays.stream(files2).forEach(file2 -> {
					ZipUtil.unzip(file);
				});
			}
		});

		List<File> fileList = FileUtil.loopFiles(tempFile);

		List<File> fsnFileList = fileList.stream().filter(one -> {
			return FileUtil.extName(one).equalsIgnoreCase("fsn");
		}).collect(Collectors.toList());

		File descDir = new File(desc);

		if (HelpMe.isNotNull(fsnNameArr)) {
			List<String> fsnNameList = Arrays.asList(fsnNameArr);
			fsnFileList.stream().forEach(one -> {
				String[] nameArr = one.getName().split("_");
				if (HelpMe.isNotNull(nameArr)) {
					if (fsnNameList.contains(nameArr[0])) {
						FileUtil.copy(one, descDir, true);
					}
				}
			});
		} else {
			fsnFileList.stream().forEach(one -> {
				FileUtil.copy(one, descDir, true);
			});
		}

		FileUtil.del(tempFile);
	}


	/**
	 * 在 base 目录下，生成dir目录
	 *
	 * @param base
	 * @param dir
	 * @return
	 */
	public static String createDir(String base, String dir) {
		String newDir = base + File.separator + dir;
		if (!FileUtil.exist(newDir)) {
			FileUtil.mkdir(newDir);
		}
		return newDir;
	}


	/**
	 * 提取 path 文件夹下的所有 zip 包到 dest 文件夹下
	 *
	 * @param path
	 * @param dest
	 */
	public static void extractZipFromPath(String path, String dest) {
		extractFileFromPath(path, dest, "zip");
	}


	/**
	 * 提取 path 文件夹下的所有后缀为 extName 的文件到 dest 文件夹下
	 *
	 * @param path
	 * @param dest
	 * @param extName 后缀名
	 */
	public static void extractFileFromPath(String path, String dest, String extName) {
		if (!FileUtil.isDirectory(path)) {
			throw new RuntimeException(path + "不是目录！");
		}
		if (!FileUtil.isDirectory(dest)) {
			throw new RuntimeException(dest + "不是目录！");
		}
		List<File> fileList = FileUtil.loopFiles(path);
		fileList.stream().forEach(file -> {
			if (!file.isDirectory()) {
				String extName2 = FileUtil.extName(file);
				if (extName2.equalsIgnoreCase(extName)) {
					FileUtil.copy(file, new File(dest), true);
				}
			}
		});
	}


	/**
	 * 通过捆编码，查询捆fsn压缩文件的路径
	 *
	 * @param fsnList
	 * @param bundleCode
	 */
	public static String fsnPathByBundleCode(List<File> fsnList, String bundleCode) {
		String bundleFsnPath = "";
		for (File fsn : fsnList) {
			String fsnName = fsn.getName();
			String code = fsnName.split("_")[0];
			if (FileUtil.extName(fsn).equalsIgnoreCase("zip")) {
				if (bundleCode.equalsIgnoreCase(code)) {
					return fsn.getPath();
				}
			}
		}
		return bundleFsnPath;
	}


	/**
	 *
	 * @param machineSno	机具编码
	 * @param moneyFlag		币种	，如：CNY
	 * @param valuta	币值	，如：100，50，20
	 * @return
	 */
	public static FsnRow getFsnRow(String machineSno,String moneyFlag,int valuta) {
		FsnRow row = new FsnRow();



		return row;
	}



	/**
	 * @param count FSN文件中，纸币张数
	 * @param moneyFlag 币种
	 * @return
	 */
	public static FsnInfo getFsnInfo(int count,String moneyFlag,int valuta) {

		FsnInfo fsnInfo = new FsnInfo();
		int headStart[] = {20, 10, 7, 26};
		int headString[] = {0, 1, 46, 83, 78, 111};
		int headEnd[] = {0, 1, 2, 3};
		fsnInfo.setHeadStart(headStart);
		fsnInfo.setHeadString(headString);
		fsnInfo.setHeadEnd(headEnd);
		fsnInfo.setCounter(count);

		String str1 = RandomUtil.randomString(5);
		String str2 = RandomUtil.randomString(4);
		String str3 = RandomUtil.randomString(11);
		//机具编号
		String machineSno = str1 + "/" + str2 + "/" + str3;

		ArrayList<FsnRow> rowList = Lists.newArrayList();
		for (int i = 1; i <= count; i++) {
			rowList.add(getFsnRow(machineSno,moneyFlag,valuta));
		}

		fsnInfo.setFsnRows(rowList);
		return fsnInfo;
	}

	/**
	 * @param fsnFilePath 全路径，如 /home/tq/abc.fsn
	 * @param fsnInfo
	 * @return
	 */
	public static boolean writeFsn(String fsnFilePath, FsnInfo fsnInfo) {
		FsnWriter fw = new FsnWriter(fsnFilePath, fsnInfo);
		try {
			return fw.writeFsn();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}


	/**
	 *	创建HZH压缩文件包，其中（默认）包含：
	 *		1 个捆FSN文件
	 *		10个把FSN文件
	 *		1个HZH文件
	 * @param bankCode	银行编码
	 * @param opName	清分员名称
	 * @param bundleZipPath	 生成的压缩文件存储路径
	 * @param bundleGzhCount	捆FSN文件中的冠字号个数
	 * @param handleGzhCount	把FSN文件中的冠字号个数
	 * @param handleCount		生成的把数（默认10把）
	 * @return
	 */
	public static String createHzh(String bankCode,String opName,String bundleZipPath, Integer bundleGzhCount, Integer handleGzhCount,Integer... handleCount) {
		String path = bundleZipPath+"Temp";

		path = FileUtil.mkdir(new File(path)).getAbsolutePath();
		FileUtil.del(new File(bundleZipPath));

		int handleCountAgain = 10;
		if (HelpMe.isNotNull(handleCount)){
			handleCountAgain = handleCount[0];
		}

		int bundleCount = 1;
		for (int k = 1; k <= bundleCount; k++) {

			ArrayList<FsnRow> arrayList = Lists.newArrayList();
			Date date = new Date();
			/**
			 * 生成10个把fsn文件
			 */
			for (int i = 0; i < handleCountAgain; i++) {
				FsnInfo fsnInfo = getFsnInfo(handleGzhCount,"CNY",100);

				String numStr = RandomUtil.randomNumbers(14);

				String fileName = "HGRICBC" + numStr + "_" + DateUtil.format(date, "yyyyMMddHHmmss");
				String fsnFileName = fileName + ".FSN";
				String writePath = bundleZipPath + File.separatorChar + fsnFileName;
				writeFsn(writePath, fsnInfo);

				arrayList.addAll(fsnInfo.getFsnRows());
			}


			/**
			 * 生成捆fsn文件
			 */
			String numStr = RandomUtil.randomNumbers(14);
			String ksno = "KGRICBC" + numStr;
			{
				FsnInfo fsnInfo = getFsnInfo(bundleGzhCount,"CNY",100);
				fsnInfo.setFsnRows(arrayList);

				String fileName = ksno + "_" + DateUtil.format(date, "yyyyMMddHHmmss");
				String fsnFileName = fileName + ".FSN";
				String writePath = bundleZipPath + File.separatorChar + fsnFileName;
				writeFsn(writePath, fsnInfo);
			}


			/**
			 * 生成 hzh 文件
			 */
			String zipPath = path + File.separatorChar + "handfulZip";
			String zipName = bankCode+"_"+ksno+"_"+DateUtil.format(date,"yyyyMMddHHmmss")+".ZIP";

			{
//				20180921164842:C1092133000016:C1092133000016:3:0:T:7:李翠花:SPEED/GUAO/0:1:KJHG1234567891:CNY:0
				StringBuffer hzhContent = new StringBuffer();
				hzhContent.append(DateUtil.format(date,"yyyyMMddHHmmss")+":");
				hzhContent.append(bankCode+":");
				hzhContent.append(bankCode+":");
				hzhContent.append("3:");
				hzhContent.append("0:");
				hzhContent.append("T:");
				hzhContent.append("7:");
				hzhContent.append(opName+":");
				hzhContent.append("SPEED/AUTO:");
				hzhContent.append("1:");
				hzhContent.append(ksno+":");
				hzhContent.append("CNY:");
				hzhContent.append("0");
				String hzhFileName = ksno+"_"+DateUtil.format(date,"yyyyMMddHHmmss")+".HZH";
				String writePath = bundleZipPath+File.separatorChar + bankCode+"_"+hzhFileName;
				FileUtil.writeString(hzhContent.toString(),new File(writePath),"UTF-8");
			}

			if (!FileUtil.exist(zipPath)) {
				FileUtil.mkdir(zipPath);
			}

			{
				OutputStream fos1 = null;
				try {
					fos1 = new FileOutputStream(new File(zipPath + File.separatorChar + zipName));
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				HelpMe.toZip(bundleZipPath, fos1, false);

				FileUtil.del(bundleZipPath);
				FileUtil.mkdir(bundleZipPath);

				//最后一次循环
				if (k == bundleCount) {
					File[] fileArr = FileUtil.ls(zipPath);
					for (int j = 0; j < fileArr.length; j++) {
						FileUtil.move(fileArr[j], new File(bundleZipPath), true);
					}
					FileUtil.del(zipPath);
				}
			}
		}

		FileUtil.del(new File(path));

		return bundleZipPath;
	}



}
