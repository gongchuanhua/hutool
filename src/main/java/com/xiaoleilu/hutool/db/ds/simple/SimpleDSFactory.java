package com.xiaoleilu.hutool.db.ds.simple;

import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import com.xiaoleilu.hutool.db.ds.DSFactory;
import com.xiaoleilu.hutool.exceptions.DbRuntimeException;
import com.xiaoleilu.hutool.io.IoUtil;
import com.xiaoleilu.hutool.setting.Setting;
import com.xiaoleilu.hutool.util.CollectionUtil;
import com.xiaoleilu.hutool.util.StrUtil;

/**
 * 简单数据源工厂类
 * @author Looly
 *
 */
public class SimpleDSFactory extends DSFactory {
	
	private Setting setting;
	/** 数据源池 */
	private Map<String, SimpleDataSource> dsMap;
	
	public SimpleDSFactory() {
		this(null);
	}
	
	public SimpleDSFactory(Setting setting) {
		super("Hutool-Simple-Datasource");
		if(null == setting){
			setting = new Setting(DEFAULT_DB_SETTING_PATH, true);
		}
		this.setting = setting;
		this.dsMap = new ConcurrentHashMap<>();
	}

	@Override
	public DataSource getDataSource(String group) {
		// 如果已经存在已有数据源（连接池）直接返回
		final SimpleDataSource existedDataSource = dsMap.get(group);
		if (existedDataSource != null) {
			return existedDataSource;
		}

		final SimpleDataSource ds = createDataSource(group);
		// 添加到数据源池中，以备下次使用
		dsMap.put(group, ds);
		return ds;
	}

	@Override
	public void close(String group) {
		if (group == null) {
			group = StrUtil.EMPTY;
		}

		SimpleDataSource ds = dsMap.get(group);
		if (ds != null) {
			IoUtil.close(ds);
			dsMap.remove(group);
		}
	}

	@Override
	public void destroy() {
		if(CollectionUtil.isNotEmpty(dsMap)){
			Collection<SimpleDataSource> values = dsMap.values();
			for (SimpleDataSource ds : values) {
				IoUtil.close(ds);
			}
			dsMap.clear();
			dsMap = null;
		}
	}

	/**
	 * 创建数据源
	 * @param group 分组
	 * @return 简单数据源 {@link SimpleDataSource}
	 */
	private SimpleDataSource createDataSource(String group){
		Properties config = setting.getProperties(group);
		if(CollectionUtil.isEmpty(config)){
			throw new DbRuntimeException("No HikariCP config for group: [{}]", group);
		}
		final SimpleDataSource ds = new SimpleDataSource(setting, group);
		return ds;
	}
}
