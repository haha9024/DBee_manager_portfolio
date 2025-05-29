package com.manager.dbee.config;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

/*
 * LogDataBase 관련 설정 클래스
 */
@Configuration
@MapperScan(basePackages="com.manager.dbee.dao.log", sqlSessionFactoryRef="logSqlSessionFactory")
public class LogDBConfig {
	
	@Bean(name="logDataSource")
	@ConfigurationProperties(prefix="spring.datasource.log")	//application.properties 파일에 정의한 대로
	DataSource dataSource() {
		return DataSourceBuilder.create().build();
	}
	
	@Bean(name="logSqlSessionFactory")
	@ConfigurationProperties
	SqlSessionFactory sqlSessionFactory(@Qualifier("logDataSource") DataSource dataSource) throws Exception{
		SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
		sqlSessionFactoryBean.setDataSource(dataSource);
	    
		// 다중 DB 설정 시, 이거 없으면 쿼리실행문/결과 console에 로깅 안 됨
		org.apache.ibatis.session.Configuration config = new org.apache.ibatis.session.Configuration();
        config.setLogImpl(org.apache.ibatis.logging.stdout.StdOutImpl.class);  // ✅ 필수
        sqlSessionFactoryBean.setConfiguration(config);
		
		sqlSessionFactoryBean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath:mapper/log/*.xml"));
	    return sqlSessionFactoryBean.getObject();

	}
	
	@Bean(name="testTransactionManager")
	DataSourceTransactionManager transactionManager(@Qualifier("logDataSource") DataSource dataSource) {
		return new DataSourceTransactionManager(dataSource);
	}
}
