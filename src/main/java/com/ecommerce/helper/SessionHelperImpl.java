package com.ecommerce.helper;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.*;

@Service
public class SessionHelperImpl implements SessionHelper{

    @Override
    public void removeSessionMessage() {
        removeSessionAttribute("message"); // keep old behavior for backward compatibility
    }

    public void removeSessionAttribute(String attributeName) {
        try {
            System.out.println("Removing attribute: " + attributeName + " from session");
            HttpSession session = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest().getSession();
            session.removeAttribute(attributeName);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}

