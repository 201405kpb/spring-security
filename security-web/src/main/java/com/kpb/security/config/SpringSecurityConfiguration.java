package com.kpb.security.config;

import com.kpb.security.service.EmailService;
import com.kpb.security.service.SmsService;
import com.kpb.security.service.WeChatService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.regex.Pattern;


@EnableMethodSecurity
@EnableWebSecurity(debug = true)
@Configuration
public class SpringSecurityConfiguration {

    private static final String NOOP_PASSWORD_PREFIX = "{noop}";

    private static final Pattern PASSWORD_ALGORITHM_PATTERN = Pattern.compile("^\\{.+}.*$");

    private static final Log logger = LogFactory.getLog(SpringSecurityConfiguration.class);
    private final EmailService emailService;
    private final SmsService smsService;
    private final WeChatService wechatService;

    public SpringSecurityConfiguration(EmailService emailService, SmsService smsService, WeChatService wechatService) {
        this.emailService = emailService;
        this.smsService = smsService;
        this.wechatService = wechatService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		return http
				.formLogin()
				.loginPage("/login")
				//.failureUrl("/login_fail")
				.failureHandler(customSimpleUrlAuthenticationFailureHandler())
				//.defaultSuccessUrl("/index",true)
				.successHandler(customAuthenticationSuccessHandler())
				.permitAll()
				.and()
				.logout()
				//.logoutSuccessUrl("/logout_success")
				.logoutSuccessHandler(customLogoutSuccessHandler())
				.permitAll()
				.and()
				.authorizeHttpRequests()
				.requestMatchers("/login_fail", "/login", "/logout_success", "/css/**", "/js/**", "/plugins/**", "/images/**", "/fonts/**") //解决静态资源被拦截的问题
				.permitAll()
				.anyRequest()
				.authenticated() //若要给应用程序发送请求，则发送请求的用户必须先通过认证
				.and()
				.csrf().disable()
				.build();

	}


    @Bean
    public WebSecurityCustomizer BuildWebSecurityCustomizer() {
        return web -> {
        };
    }


    public AuthenticationSuccessHandler customAuthenticationSuccessHandler() {
        CustomSavedRequestAwareAuthenticationSuccessHandler customSavedRequestAwareAuthenticationSuccessHandler = new CustomSavedRequestAwareAuthenticationSuccessHandler();
        customSavedRequestAwareAuthenticationSuccessHandler.setDefaultTargetUrl("/index");
        customSavedRequestAwareAuthenticationSuccessHandler.setEmailService(emailService);
        customSavedRequestAwareAuthenticationSuccessHandler.setSmsService(smsService);
        customSavedRequestAwareAuthenticationSuccessHandler.setWeChatService(wechatService);
        return customSavedRequestAwareAuthenticationSuccessHandler;
    }

    public LogoutSuccessHandler customLogoutSuccessHandler() {
        CustomLogoutSuccessHandler customLogoutSuccessHandler = new CustomLogoutSuccessHandler();
        customLogoutSuccessHandler.setDefaultTargetUrl("/logout_success");
        customLogoutSuccessHandler.setEmailService(emailService);
        customLogoutSuccessHandler.setSmsService(smsService);
        customLogoutSuccessHandler.setWeChatService(wechatService);
        return customLogoutSuccessHandler;
    }

    public AuthenticationFailureHandler customSimpleUrlAuthenticationFailureHandler() {
        CustomSimpleUrlAuthenticationFailureHandler customSimpleUrlAuthenticationFailureHandler = new CustomSimpleUrlAuthenticationFailureHandler();
        customSimpleUrlAuthenticationFailureHandler.setDefaultFailureUrl("/login_fail");
        customSimpleUrlAuthenticationFailureHandler.setEmailService(emailService);
        customSimpleUrlAuthenticationFailureHandler.setSmsService(smsService);
        customSimpleUrlAuthenticationFailureHandler.setWeChatService(wechatService);
        return customSimpleUrlAuthenticationFailureHandler;
    }
    @Bean
    public UserDetailsService customUserDetailsService(SecurityProperties properties,
                                                       ObjectProvider<PasswordEncoder> passwordEncoder) {

        return username -> {
            SecurityProperties.User sysUser = properties.getUser();
            List<String> roles = sysUser.getRoles();
            return User.builder()
                    .username(sysUser.getName())
                    .password(getOrDeducePassword(sysUser, passwordEncoder.getIfAvailable()))
                    .roles(StringUtils.toStringArray(roles))
                    .build();
        };
    }

    private String getOrDeducePassword(SecurityProperties.User user, PasswordEncoder encoder) {
        String password = user.getPassword();
        if (user.isPasswordGenerated()) {
            logger.warn(String.format(
                    "%n%nUsing generated security password: %s%n%nThis generated password is for development use only. "
                            + "Your security configuration must be updated before running your application in "
                            + "production.%n",
                    user.getPassword()));
        }
        if (encoder != null && PASSWORD_ALGORITHM_PATTERN.matcher(password).matches()) {
            return encoder.encode(password);
        }
        return NOOP_PASSWORD_PREFIX + password;
    }

    @Bean
    public PasswordEncoder customPasswordEncoder(){

        return  new BCryptPasswordEncoder();
    }

}
