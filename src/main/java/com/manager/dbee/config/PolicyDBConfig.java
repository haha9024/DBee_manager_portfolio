package com.manager.dbee.config;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
//import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

@Configuration
@MapperScan(basePackages="com.manager.dbee.dao.policy")
public class PolicyDBConfig {
	
	@Primary
	@Bean(name="policyDataSource")
	@ConfigurationProperties(prefix="spring.datasource.policy")	//application.properties 파일에 정의한 대로
	DataSource dataSource() {
		return DataSourceBuilder.create().build();
	}
	
	@Primary
	@Bean(name="policySqlSessionFactory")
	SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception{
		SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
		sqlSessionFactoryBean.setDataSource(dataSource);
	    
		// 다중 DB 설정 시, 이거 없으면 쿼리실행문/결과 console에 로깅 안 됨
		org.apache.ibatis.session.Configuration config = new org.apache.ibatis.session.Configuration();
        config.setLogImpl(org.apache.ibatis.logging.stdout.StdOutImpl.class);  // ✅ 필수
        sqlSessionFactoryBean.setConfiguration(config);
		
		sqlSessionFactoryBean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath:mapper/policy/*.xml"));
	    return sqlSessionFactoryBean.getObject();

	}
	
	@Primary
	@Bean(name="policyTransactionManager")
	DataSourceTransactionManager transactionManager(DataSource dataSource) {
		return new DataSourceTransactionManager(dataSource);
	}
}
