<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN"
            "http://www.w3.org/TR/html4/strict.dtd">
            
<jsp:directive.page errorPage="/WEB-INF/content/ErrorPage.jsp"/>
<jsp:scriptlet>
    String handler=""; 
    if (request.isUserInRole("admin")) {
        handler = "/admin";
    }
    else if (request.isUserInRole("user")) {
        handler = "/user";
    }
    String action=request.getContextPath() + 
        "/model" + handler + "/handler";
    String principal = (request.getUserPrincipal() != null) ?
        request.getUserPrincipal().getName() : "'null'";
</jsp:scriptlet>        
<html>
    <title><%=principal%> Main Menu</title>
    <body>
        <h2><%=principal%> Main Menu</h2>
    
        You will have luck with these<p/>    
        <form method="GET" action="<%=action%>">
            <input type="submit" name="command" value="logout"/>
        </form>
        <form method="GET" action="<%=action%>">
            <input type="text" name="role" readonly="true" value="admin"/>
            <input type="submit" name="command" value="isCallerInRole"/>
        </form>
        <form method="GET" action="<%=action%>">
            <input type="text" name="role" readonly="true" value="user"/>
            <input type="submit" name="command" value="isCallerInRole"/>
        </form>
        <form method="GET" action="<%=action%>">
            <input type="text" name="role" readonly="true" value="internalRole"/>
            <input type="submit" name="command" value="isCallerInRole"/>
        </form>
        <form method="GET" action="<%=action%>">
            <input type="text" name="role" value=""/>
            <input type="submit" name="command" value="isCallerInRole"/>
        </form>
        <form method="GET" action="<%=action%>">
            <input type="submit" name="command" value="pingAll"/>
        </form>
        
        <jsp:scriptlet>
            if (!request.isUserInRole("user")) {
        </jsp:scriptlet>
            You are not a user, so you're not going to have luck with this<p/>
        <jsp:scriptlet>
            } else {
        </jsp:scriptlet>
            You are have the user role, so this should work<p/>
        <jsp:scriptlet>
            } 
        </jsp:scriptlet>

        <form method="GET" action="<%=action%>">
            <input type="submit" name="command" value="pingUser"/>
        </form>
        

        <jsp:scriptlet>
            if (!request.isUserInRole("admin")) {
        </jsp:scriptlet>
        You are not an admin, so you're not going to have luck with this<p/>
        <jsp:scriptlet>
            } else {
        </jsp:scriptlet>
        You are have the admin role, so this should work<p/>
        <jsp:scriptlet>
            } 
        </jsp:scriptlet>
        
            <form method="GET" action="<%=action%>">
                <input type="submit" name="command" value="pingAdmin"/>
            </form>
        
        <p/><a href="<%=request.getContextPath()%>/index.jsp">Go to Main Page</a>        
    </body>
</html>
            
