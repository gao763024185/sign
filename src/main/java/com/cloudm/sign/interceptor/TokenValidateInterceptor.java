package com.cloudm.sign.interceptor;

import com.cloudm.framework.common.ex.BusinessCheckFailException;
import com.cloudm.framework.common.util.StringUtil;
import com.cloudm.framework.common.web.result.Result;
import com.cloudm.sign.annotation.CaptureToken;
import com.cloudm.sign.dao.ApplicationSessionContext;
import com.cloudm.sign.enums.TokenErrorEnum;
import com.cloudm.sign.token.DefaultTokenHelper;
import com.google.gson.Gson;
import com.cloudm.sign.annotation.NotCertification;
import com.cloudm.sign.constant.Constants;
import com.cloudm.sign.context.DefaultContextHolder;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Jay
 * @version v1.0
 * @description 拦截器用于拦截所有的请求校验token
 * @date 2018-03-01 14:15
 */
@Slf4j
public class TokenValidateInterceptor<S extends ApplicationSessionContext> extends HandlerInterceptorAdapter {

    @Setter
    private DefaultTokenHelper<S> defaultTokenHelper;

    @Setter
    private Boolean tokenEnable;

    @Setter
    private DefaultContextHolder<S> defaultContextHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        try {
            // 如果打了该注解是表示不需要进行登陆认证的的，直接放行
            NotCertification notCertification = ((HandlerMethod) handler).getMethodAnnotation(NotCertification.class);
            // 在以下2种情况下放行 1、关闭开关时不需要token认证的 2、开启@NotCertification不需要token认证
            if(!tokenEnable || notCertification != null){
                return super.preHandle(request, response, handler);
            }
            //如果打了该注解表示即可登陆访问亦可不登录访问
            CaptureToken captureToken = ((HandlerMethod) handler).getMethodAnnotation(CaptureToken.class);
            String token = request.getHeader(Constants.TOKEN);
            //如果打了该注解，并且token信息为空则表示，此时无需登录直接访问
            if (captureToken != null && StringUtil.isEmpty(token)){
                return super.preHandle(request, response, handler);
            }
            if (StringUtils.isEmpty(token)) {
                throw new BusinessCheckFailException(TokenErrorEnum.TOKEN_OVER_DUE_ERROR.getKey(), TokenErrorEnum.TOKEN_OVER_DUE_ERROR.getName());
            }
            // 验证token
            defaultTokenHelper.verifyToken(token);
        } catch (Exception e) {
            response.setHeader("contentType", "text/html; charset=utf-8");
            response.setContentType("text/html;charset=utf-8");
            response.setCharacterEncoding("utf-8");
            response.getWriter().write(new Gson().toJson(this.writeResponse(e)));
            return Boolean.FALSE;
        }
        return super.preHandle(request, response, handler);
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response,
                           Object handler, ModelAndView modelAndView) throws Exception {
        super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
        // 清空业务session
        defaultContextHolder.resetContext();
    }

    /**
     * token验证失败返回的response
     *
     * @param e
     */
    private Result writeResponse(Exception e) {
        Result result = new Result();
        result.setCode(TokenErrorEnum.TOKEN_OVER_DUE_ERROR.getKey());
        result.setSuccess(Boolean.FALSE);
        result.setDevMsg(e.getMessage());
        return result;
    }
}
