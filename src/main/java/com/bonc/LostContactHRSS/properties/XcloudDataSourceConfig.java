package com.bonc.LostContactHRSS.properties;

import com.bonc.commons.jdbc.datasource.EncryptPooledDataSource;
import com.bonc.commons.jdbc.datasource.SingleDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

/**
 * @author malin
 */
@Configuration
@MapperScan(basePackages = "com.bonc.LostContactHRSS.mapper.xcloud",sqlSessionTemplateRef = "xcloudSqlSessionTemplate")
public class XcloudDataSourceConfig {


    @Value("${spring.xcloud.url}")
    private String dbUrl;

    @Value("${spring.xcloud.username}")
    private String username;

    @Value("${spring.xcloud.password}")
    private String password;

    @Value("${spring.xcloud.driver-class-name}")
    private String driverClassName;

    @Value("${spring.xcloud.initialPoolSize}")
    private int initialPoolSize;

    @Value("${spring.xcloud.acquireIncrement}")
    private int acquireIncrement;

    @Value("${spring.xcloud.minPoolSize}")
    private int minPoolSize;

    @Value("${spring.xcloud.maxPoolSize}")
    private int maxPoolSize;

    @Value("${spring.xcloud.maxIdleTime}")
    private int maxIdleTime;

    @Value("${spring.xcloud.encryptType}")
    private int encryptType;

    /**
     * xcloud数据源,使用连接池
     */
    @Bean(name="xcloudDataSource")
    public DataSource primaryDataSource() throws Exception {
        EncryptPooledDataSource dataSource = new EncryptPooledDataSource();
        dataSource.setJdbcUrl(dbUrl);
        dataSource.setUser(username);
        dataSource.setPassword(password);
        dataSource.setDriverClass(driverClassName);
        dataSource.setInitialPoolSize(initialPoolSize);
        dataSource.setAcquireIncrement(acquireIncrement);
        dataSource.setMinPoolSize(minPoolSize);
        dataSource.setMaxPoolSize(maxPoolSize);
        dataSource.setMaxIdleTime(maxIdleTime);
        dataSource.setEncryptType(encryptType);
        SingleDataSource dataSource1 = new SingleDataSource();
        dataSource1.setInternalDataSource(dataSource);
        return dataSource1;
    }

    @Bean(name = "xcloudSqlSessionFactory")
    public SqlSessionFactory testSqlSessionFactory(@Qualifier("xcloudDataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        bean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath:mybatis/XcloudMapper.xml"));
        return bean.getObject();
    }

    @Bean(name = "xcloudTransactionManager")
    public DataSourceTransactionManager testTransactionManager(@Qualifier("xcloudDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean(name = "xcloudSqlSessionTemplate")
    public SqlSessionTemplate testSqlSessionTemplate(@Qualifier("xcloudSqlSessionFactory") SqlSessionFactory sqlSessionFactory) throws Exception {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

}
