package com.jd.decoration.ai.service.util;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

@Slf4j
@Component
public class SpringBeanUtil implements ApplicationContextAware {

    private static ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        SpringBeanUtil.context = context;
    }

    /**
     * @param clazz class
     * @param <T>   泛型
     * @return bean
     * @desc 通过class获取Bean
     */
    public static <T> T getBean(Class<T> clazz) {
        return context.getBean(clazz);
    }

    /**
     * 根据bean名字和类型获取实例
     *
     * @param beanName 名字
     * @param clazz    类型
     * @param <T>      类型T
     * @return 类型实例
     */
    public static <T> Optional<T> getBean(String beanName, Class<T> clazz) {
        if (StrUtil.isNotBlank(beanName)) {
            log.warn("类名字为空");
            return Optional.empty();
        }
        if (Objects.isNull(clazz)) {
            log.warn("类型为空");
            return Optional.empty();
        }
        if (Objects.isNull(context)) {
            log.warn("spring 加载失败");
            return Optional.empty();
        }
        return Optional.of(context.getBean(beanName, clazz));
    }
}