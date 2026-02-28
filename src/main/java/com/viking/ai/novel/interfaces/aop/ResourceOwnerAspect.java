package com.viking.ai.novel.interfaces.aop;

import com.viking.ai.novel.application.service.ChapterService;
import com.viking.ai.novel.application.service.CurrentUserService;
import com.viking.ai.novel.application.service.NovelService;
import com.viking.ai.novel.application.service.NovelVectorService;
import com.viking.ai.novel.application.service.UserVectorService;
import com.viking.ai.novel.domain.model.Chapter;
import com.viking.ai.novel.domain.model.Novel;
import com.viking.ai.novel.domain.model.NovelVector;
import com.viking.ai.novel.domain.model.UserVector;
import com.viking.ai.novel.infrastructure.excep.ForbiddenException;
import com.viking.ai.novel.infrastructure.excep.NotLoggedInException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * 全局越权校验切面：根据方法上的注解校验登录态与资源归属，未登录抛 NotLoggedInException，越权抛 ForbiddenException。
 */
@Aspect
@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class ResourceOwnerAspect {

    private final CurrentUserService currentUserService;
    private final NovelService novelService;
    private final ChapterService chapterService;
    private final UserVectorService userVectorService;
    private final NovelVectorService novelVectorService;

    @Before("""
        @annotation(com.viking.ai.novel.interfaces.aop.RequireLogin) ||
        @annotation(com.viking.ai.novel.interfaces.aop.CheckNovelOwner) ||
        @annotation(com.viking.ai.novel.interfaces.aop.CheckChapterOwner) ||
        @annotation(com.viking.ai.novel.interfaces.aop.CheckUserId) ||
        @annotation(com.viking.ai.novel.interfaces.aop.CheckNovelIdOwner) ||
        @annotation(com.viking.ai.novel.interfaces.aop.CheckRequestNovelId) ||
        @annotation(com.viking.ai.novel.interfaces.aop.CheckUserVectorOwner) ||
        @annotation(com.viking.ai.novel.interfaces.aop.CheckNovelVectorOwner)
        """)
    public void checkResourceOwner(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Object[] args = joinPoint.getArgs();

        HttpSession session = getSession();
        long currentUserId = currentUserService.requireCurrentUserId(session);

        if (method.isAnnotationPresent(CheckNovelOwner.class)) {
            CheckNovelOwner ann = method.getAnnotation(CheckNovelOwner.class);
            Long novelId = getLongArg(args, ann.paramIndex());
            if (novelId != null && !novelBelongsTo(novelId, currentUserId)) {
                throw new ForbiddenException("无权限操作该小说");
            }
        }
        if (method.isAnnotationPresent(CheckNovelIdOwner.class)) {
            CheckNovelIdOwner ann = method.getAnnotation(CheckNovelIdOwner.class);
            Long novelId = getLongArg(args, ann.paramIndex());
            if (novelId != null && !novelBelongsTo(novelId, currentUserId)) {
                throw new ForbiddenException("无权限操作该小说");
            }
        }
        if (method.isAnnotationPresent(CheckRequestNovelId.class)) {
            CheckRequestNovelId ann = method.getAnnotation(CheckRequestNovelId.class);
            Long novelId = getLongFromField(args, ann.paramIndex(), ann.field());
            if (novelId != null && !novelBelongsTo(novelId, currentUserId)) {
                throw new ForbiddenException("无权限操作该小说");
            }
        }
        if (method.isAnnotationPresent(CheckChapterOwner.class)) {
            CheckChapterOwner ann = method.getAnnotation(CheckChapterOwner.class);
            Long chapterId = getLongArg(args, ann.paramIndex());
            if (chapterId != null && !chapterBelongsToCurrentUser(chapterId, currentUserId)) {
                throw new ForbiddenException("无权限操作该章节");
            }
        }
        if (method.isAnnotationPresent(CheckUserId.class)) {
            CheckUserId ann = method.getAnnotation(CheckUserId.class);
            Long userId = getLongArg(args, ann.paramIndex());
            if (userId != null && userId.longValue() != currentUserId) {
                throw new ForbiddenException("无权限");
            }
        }
        if (method.isAnnotationPresent(CheckUserVectorOwner.class)) {
            CheckUserVectorOwner ann = method.getAnnotation(CheckUserVectorOwner.class);
            Long vectorId = getLongArg(args, ann.paramIndex());
            if (vectorId != null && !userVectorBelongsTo(vectorId, currentUserId)) {
                throw new ForbiddenException("无权限操作该资料库");
            }
        }
        if (method.isAnnotationPresent(CheckNovelVectorOwner.class)) {
            CheckNovelVectorOwner ann = method.getAnnotation(CheckNovelVectorOwner.class);
            Long nvId = getLongArg(args, ann.paramIndex());
            if (nvId != null && !novelVectorBelongsTo(nvId, currentUserId)) {
                throw new ForbiddenException("无权限操作该关联");
            }
        }
    }

    private static HttpSession getSession() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) return null;
        HttpServletRequest request = attrs.getRequest();
        return request != null ? request.getSession(false) : null;
    }

    private static Long getLongArg(Object[] args, int index) {
        if (args == null || index < 0 || index >= args.length) return null;
        Object o = args[index];
        if (o == null) return null;
        if (o instanceof Long) return (Long) o;
        if (o instanceof Number) return ((Number) o).longValue();
        return null;
    }

    private static Long getLongFromField(Object[] args, int paramIndex, String fieldName) {
        if (args == null || paramIndex < 0 || paramIndex >= args.length) return null;
        Object bean = args[paramIndex];
        if (bean == null) return null;
        try {
            Method getter = bean.getClass().getMethod("get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1));
            Object v = getter.invoke(bean);
            if (v == null) return null;
            if (v instanceof Long) return (Long) v;
            if (v instanceof Number) return ((Number) v).longValue();
            return null;
        } catch (Exception e) {
            log.warn("Reflection get {} from param {} failed: {}", fieldName, paramIndex, e.getMessage());
            return null;
        }
    }

    private boolean novelBelongsTo(Long novelId, long userId) {
        Optional<Novel> opt = novelService.getNovelById(novelId);
        return opt.isPresent() && userId == opt.get().getUserId();
    }

    private boolean chapterBelongsToCurrentUser(Long chapterId, long userId) {
        Optional<Chapter> ch = chapterService.getChapterById(chapterId);
        if (ch.isEmpty()) return false;
        Optional<Novel> novel = novelService.getNovelById(ch.get().getNovelId());
        return novel.isPresent() && userId == novel.get().getUserId();
    }

    private boolean userVectorBelongsTo(Long vectorId, long userId) {
        return userVectorService.getById(vectorId)
                .map(v -> v.getUserId() != null && v.getUserId() == userId)
                .orElse(false);
    }

    private boolean novelVectorBelongsTo(Long nvId, long userId) {
        Optional<NovelVector> nv = novelVectorService.getById(nvId);
        if (nv.isEmpty()) return false;
        return novelBelongsTo(nv.get().getNovelId(), userId);
    }
}
