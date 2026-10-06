package com.example.ocs.integration.config;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import javax.sql.DataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration(proxyBeanMethods = false)
@MapperScan(
    basePackages = "com.hospital.outpatient.mapper",
    sqlSessionTemplateRef = "hospitalSqlSessionTemplate"
)
public class HospitalMyBatisConfig {
  @Bean
  public SqlSessionFactory hospitalSqlSessionFactory(
      @Qualifier("dataSource") DataSource dataSource) throws Exception {
    MybatisSqlSessionFactoryBean factory = new MybatisSqlSessionFactoryBean();
    factory.setDataSource(dataSource);
    factory.setTypeAliasesPackage("com.hospital.outpatient.entity");
    factory.setConfiguration(mybatisConfiguration());
    return factory.getObject();
  }

  @Bean
  public SqlSessionTemplate hospitalSqlSessionTemplate(
      @Qualifier("hospitalSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
    return new SqlSessionTemplate(sqlSessionFactory);
  }

  @Bean
  public PlatformTransactionManager hospitalTransactionManager(
      @Qualifier("transactionManager") PlatformTransactionManager transactionManager) {
    return transactionManager;
  }

  private MybatisConfiguration mybatisConfiguration() {
    MybatisConfiguration configuration = new MybatisConfiguration();
    configuration.setMapUnderscoreToCamelCase(true);
    return configuration;
  }
}
