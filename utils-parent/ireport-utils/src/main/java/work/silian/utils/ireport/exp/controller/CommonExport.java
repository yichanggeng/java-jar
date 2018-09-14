package work.silian.utils.ireport.exp.controller;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.QueryParam;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleXlsReportConfiguration;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import work.silian.utils.ireport.AbstractGetIreportData;

import com.alibaba.fastjson.JSONObject;



/**
 * 公共导出excel
 * @author changgeng.yi
 *
 */
@Controller
@RequestMapping("/commonExport")
public class CommonExport {
	
	/**
	 * 默认获取导出数据的方法名
	 */
	public static final String DEFAULT_DATA_METHOD = "getIreportData";
	
	private AbstractGetIreportData ireportData = null;

	
	@RequestMapping(value="/post/exportExcel", method=RequestMethod.POST)
	public void printPDFByPost(  HttpServletRequest request, HttpServletResponse response ){
		String parameters = request.getParameter("parameters");
		this.ExportExcel(parameters  ,request , response);
	}
	
	
	
	
	
	/**
	 * 
	 * @param request
	 * @param response
	 * @param jasper jsper文件名称
	 * @param parameters 参数json字符串 必填项 ：className
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value="/get/exportExcel" , method=RequestMethod.GET)
	public void ExportExcel( @QueryParam("parameters") String parameters , HttpServletRequest request, HttpServletResponse response ){
		
		try {
			//解析json字符串
			Map<String ,Object> paramsMap = JSONObject.parseObject(parameters, Map.class);
			//获取 数据查询类，获取需要打印的数据
			String className = paramsMap.get("className") == null ?
												null : paramsMap.get("className").toString();
			String methodName = paramsMap.get("methodName") == null ?
												CommonExport.DEFAULT_DATA_METHOD : paramsMap.get("methodName").toString();;
			if(className == null ){
				//没有传入类名，则直接返回
				return ;
			}
			
			//通过spring获取类实例
			WebApplicationContext webApplicationContext = ContextLoader.getCurrentWebApplicationContext();    
			ireportData =  webApplicationContext.getBean(className , AbstractGetIreportData.class);
			Method classMethod = ireportData.getClass().getMethod(methodName,  Map.class);
			//获取数据
			paramsMap = (Map<String, Object>) classMethod.invoke( ireportData, paramsMap);
			
			// 设置头文件信息为excel格式
			response.setContentType("application/vnd.ms-excel"); 
			
			//设置文件名
			String fileName = null;
			if( paramsMap.get("fileName") == null){
				fileName = ( new Date().getTime() ) + ".xls";
			}else{
				fileName = new String( paramsMap.get("fileName").toString().getBytes("GBK") , "iso-8859-1" );
				//获取后 在报表传输数据中删除， 减小生成报表时内存压力
				paramsMap.remove("fileName");
			}
			//设置文件名
			response.setHeader("Content-disposition", "attachment; filename="
					+ fileName);
			
			response.setCharacterEncoding("UTF-8");
			
			URL url = Resource.class.getClassLoader().getResource("");
			
			
			
			String path = url.getPath();
			ServletOutputStream servletOutputStream = response
					.getOutputStream();
			//jasper文件名由后台传递， 减轻前台url的压力
			String jasper = path + "/iReport/" + paramsMap.get("jasper").toString();
			
			paramsMap.put("SUBREPORT_DIR",path + "/iReport/");
			
			//获取后 在报表传输数据中删除， 减小生成报表时内存压力
			paramsMap.remove("jasper");
			
			JasperReport jasperReport = (JasperReport) JRLoader.loadObject(new File(jasper));
			JasperPrint jasperPrint = null;
			//如果有list数据存储
			if( paramsMap.get("reportList") != null){
				JRDataSource jrDataSource = new JRBeanCollectionDataSource( ( List<Object> )paramsMap.get("reportList"));
				jasperPrint = JasperFillManager.fillReport( jasperReport, paramsMap ,jrDataSource );
			}else {
				jasperPrint = JasperFillManager.fillReport( jasperReport, paramsMap);
			}
			
			// 导出excel格式
			JRXlsExporter exporter = new JRXlsExporter();
			//5.6以上的打印设置
			exporter.setExporterInput( new SimpleExporterInput(jasperPrint) );
			exporter.setExporterOutput( new SimpleOutputStreamExporterOutput(servletOutputStream));
			
			SimpleXlsReportConfiguration config = new SimpleXlsReportConfiguration();
			config.setOnePagePerSheet( Boolean.TRUE );
			config.setWhitePageBackground( Boolean.FALSE );
			config.setRemoveEmptySpaceBetweenRows( Boolean.TRUE );
			config.setCollapseRowSpan( Boolean.FALSE);
			exporter.setConfiguration(config);
			
			exporter.exportReport();
			servletOutputStream.flush();
			servletOutputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JRException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		
	}
	
	
	
	
	
}
