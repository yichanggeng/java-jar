package work.silian.utils.ireport;

import java.util.Map;
/**
 * 获取报表数据的接口
 * @note  className 为类名首字母小写
 * @author changgeng.yi
 *
 */
public abstract class AbstractGetIreportData {

	/**
	 * @desc 默认获取报表数据的方法
	 * @return 返回的数据包含的值 ：
	 * 			japser(japser文件名) *
	 * 			fileName(生成文件名)	可选，不设置，则使用时间毫秒字符串
	 * 			reportList(报表中list类型)		可选，主报表中有列表时使用
	 */
	public Map<String,Object> getIreportData(Map<String ,Object> params){
		return null;
	};
	
	
	
	
}
