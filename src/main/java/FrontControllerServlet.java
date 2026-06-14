package main.java;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

public class FrontControllerServlet extends HttpServlet {
    protected void processRequest(HttpServletRequest request,HttpServletResponse response)throws ServletException, IOException {response.setContentType("text/plain");

        String contextPath = request.getContextPath();
        String uri = request.getRequestURI();
        String path = uri.substring(contextPath.length());
        PrintWriter out = response.getWriter();
        out.println("URL recue : " + uri);
        out.println("Chemin : " + path);
    }

    
    protected void doGet(HttpServletRequest request,HttpServletResponse response)throws ServletException, IOException {
                processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request,HttpServletResponse response)throws ServletException, IOException {
        processRequest(request, response);
    }
}