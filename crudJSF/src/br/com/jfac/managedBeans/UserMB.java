package br.com.jfac.managedBeans;

import java.security.Principal;

import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import br.com.jfac.facade.UserFacade;
import br.com.jfac.model.User;

@SessionScoped
@ManagedBean
public class UserMB {
	
	private User user;
	
	@EJB
	private UserFacade userFacade;

	HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
	HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
	
	public User getUser() {
		if (user == null) {
			ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
			
			String userEmail = context.getUserPrincipal().getName();
			user = userFacade.findUserByEmail(userEmail);
		}
		return user;
	}

	public boolean isUserAdmin() {
		return getRequest().isUserInRole("ADMIN");
	}
	
	public String logOut() {
		getRequest().getSession().invalidate();
		
		return "logout";
	}
	
	private HttpServletRequest getRequest() {
		return (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
	}
	
	public String login() {
		String message = "";
		String navigator = "";

		try {

			request.login(user.getName(), user.getPassword());
			Principal principal = request.getUserPrincipal();

			if (request.isUserInRole("Administrator")) {
				message = "Usuário : "
						+ principal.getName()
						+ " Você é Administrador e tem direito a todos as funcionalidades!";
				navigator = "admin";
			} else if (request.isUserInRole("Manager")) {
				message = "Usuário : "
						+ principal.getName()
						+ "Você é um Diretor e pode visualizar todos os relatórios gerenciais!";
				navigator = "manager";
			} else if (request.isUserInRole("Operator")) {
				message = "Usuário : " + principal.getName()
						+ "Você é um Operador, Por Favor Atenda bem o cliente!";
				navigator = "operator";
			}

			FacesContext.getCurrentInstance().addMessage(null,new FacesMessage(FacesMessage.SEVERITY_INFO,message, null));
			
			return navigator;
			
		} catch (ServletException e) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ocorreu algum problema e o Login Falhou!", null));
			e.printStackTrace();
		}
		
		return "failure";
	}
}