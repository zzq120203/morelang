package cn.ac.iie.Util;

import java.util.Arrays;

/**
 * 需要将数据减去均值之后在进行箱型图分析
 * 
 * @author gaoxy
 *
 *         2017年8月14日
 */
public class BoxPlots {
	public double[] data;// 减去均值后的数据
	public double QL;
	public double QU;
	public double IQR;
	public int length;
	public double expUpper;
	public double expLower;
	public double currentData;

	public BoxPlots() {
	}

	public BoxPlots(double[] data, double currentData) {
		this.setData(data, currentData);
	}

	public void setData(double[] data, double currentData) {
		this.data = data;
		this.length = data.length;
		this.currentData = currentData;
	}

	public void sort() {
		Arrays.sort(data);
	}

	public void findQ(double ratio) {
		Arrays.sort(this.data);
		double QLIndexF = (this.length + 1) / 4.0 - 1;
		double QUIndexF = (this.length + 1) / 4.0 * 3 - 1;
		int QLIndex = (int) Math.floor(QLIndexF);
		int QUIndex = (int) Math.floor(QUIndexF);
		QL = data[QLIndex] + (QLIndexF - QLIndex) * (data[QLIndex + 1] - data[QLIndex]);
		QU = data[QUIndex] + (QUIndexF - QUIndex) * (data[QUIndex + 1] - data[QUIndex]);
		IQR = QU - QL;
		expUpper = QU + ratio * IQR;
		expLower = QL - ratio * IQR;
	}

	/**
	 * judge whether this is a hot issue
	 * 
	 * @return true: is hot issue else return false
	 */
	public boolean isOutliner() {
		findQ(1.5);
		if (this.currentData > this.expUpper)
			return true;
		return false;
	}
	
	public int isOutliner(int ratio) {
	   findQ(ratio);//3
	   if (this.currentData > this.expUpper) 
		   return 1;
	   if (this.currentData < this.expLower)
		   return -1;
	   return 0;
	}
	
	
}