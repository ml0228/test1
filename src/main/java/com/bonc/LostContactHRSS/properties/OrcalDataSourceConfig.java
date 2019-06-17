package com.bonc.LostContactHRSS.properties;


import com.alibaba.druid.pool.DruidDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Properties;

/**
 * @author
 */
@Configuration
@MapperScan(basePackages = "com.bonc.LostContactHRSS.mapper.orcal",sqlSessionTemplateRef = "orcalSqlSessionTemplate")
public class OrcalDataSourceConfig {

    private Logger logger = LoggerFactory.getLogger(OrcalDataSourceConfig.class);

    @Value("${spring.orcal.url}")
    private String dbUrl;

    @Value("${spring.orcal.username}")
    private String username;

    @Value("${spring.orcal.password}")
    private String password;

    @Value("${spring.orcal.driver-class-name}")
    private String driverClassName;

    @Value("${spring.orcal.initialSize}")
    private int initialSize;

    @Value("${spring.orcal.minIdle}")
    private int minIdle;

    @Value("${spring.orcal.maxActive}")
    private int maxActive;

    @Value("${spring.orcal.maxWait}")
    private int maxWait;

    @Value("${spring.orcal.timeBetweenEvictionRunsMillis}")
    private int timeBetweenEvictionRunsMillis;

    @Value("${spring.orcal.minEvictableIdleTimeMillis}")
    private int minEvictableIdleTimeMillis;

    @Value("${spring.orcal.validationQuery}")
    private String validationQuery;

    @Value("${spring.orcal.testWhileIdle}")
    private boolean testWhileIdle;

    @Value("${spring.orcal.testOnBorrow}")
    private boolean testOnBorrow;

    @Value("${spring.orcal.testOnReturn}")
    private boolean testOnReturn;

    @Value("${spring.orcal.poolPreparedStatements}")
    private boolean poolPreparedStatements;

    @Value("${spring.orcal.maxPoolPreparedStatementPerConnectionSize}")
    private int maxPoolPreparedStatementPerConnectionSize;

    @Value("${spring.orcal.filters}")
    private String filters;

    @Value("{spring.orcal.connectionProperties}")
    private String connectionProperties;

    /**
     * orcal数据源,使用druid连接池
     */
    @Bean(name="orcalDataSource")
    @Primary
    public DataSource orcalDataSource() {
        DruidDataSource datasource = new DruidDataSource();

        datasource.setUrl(this.dbUrl);
        datasource.setUsername(username);
        datasource.setPassword(password);
        datasource.setDriverClassName(driverClassName);

        //configuration
        datasource.setInitialSize(initialSize);
        datasource.setMinIdle(minIdle);
        datasource.setMaxActive(maxActive);
        datasource.setMaxWait(maxWait);
        datasource.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
        datasource.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
        datasource.setValidationQuery(validationQuery);
        datasource.setTestWhileIdle(testWhileIdle);
        datasource.setTestOnBorrow(testOnBorrow);
        datasource.setTestOnReturn(testOnReturn);
        datasource.setPoolPreparedStatements(poolPreparedStatements);
        datasource.setMaxPoolPreparedStatementPerConnectionSize(maxPoolPreparedStatementPerConnectionSize);
        try {
            datasource.setFilters(filters);
        } catch (SQLException e) {
            logger.error("druid configuration initialization filter", e);
        }
        datasource.setConnectionProperties(connectionProperties);

        return datasource;
    }

    @Bean(name = "orcalSqlSessionFactory")
    @Primary
    public SqlSessionFactory orcalSqlSessionFactory(@Qualifier("orcalDataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        bean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath:mybatis/OrcalMapper.xml"));
        InputStream in = getClass().getResourceAsStream("/db.properties");
        Properties pro = new Properties();
        pro.load(in);
        bean.setConfigurationProperties(pro);
        in.close();
        return bean.getObject();
    }

    @Bean(name = "orcalTransactionManager")
    @Primary
    public DataSourceTransactionManager orcalTransactionManager(@Qualifier("orcalDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean(name = "orcalSqlSessionTemplate")
    @Primary
    public SqlSessionTemplate orcalSqlSessionTemplate(@Qualifier("orcalSqlSessionFactory") SqlSessionFactory sqlSessionFactory) throws Exception {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

}
