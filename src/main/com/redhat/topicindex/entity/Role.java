package com.redhat.topicindex.entity;

// Generated Aug 8, 2011 9:22:31 AM by Hibernate Tools 3.4.0.CR1

import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import static javax.persistence.GenerationType.IDENTITY;
import javax.persistence.CascadeType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.envers.Audited;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

@Entity
@Audited
@Table(name = "Role", catalog = "Skynet", uniqueConstraints = @UniqueConstraint(columnNames =
{ "RoleName" }))
public class Role implements java.io.Serializable
{
	public static final String SELECT_ALL_QUERY = "select role from Role role";
	private static final long serialVersionUID = 894929331710959265L;
	private Integer roleId;
	private String roleName;
	private String description;
	private Set<UserRole> userRoles = new HashSet<UserRole>(0);
	private Set<RoleToRole> childrenRoleToRole = new HashSet<RoleToRole>(0);
	private Set<RoleToRole> parentRoleToRole = new HashSet<RoleToRole>(0);

	public Role()
	{
	}

	public Role(String roleName)
	{
		this.roleName = roleName;
	}

	public Role(String roleName, String description, Set<UserRole> userRoles)
	{
		this.roleName = roleName;
		this.description = description;
		this.userRoles = userRoles;
	}

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "RoleID", unique = true, nullable = false)
	public Integer getRoleId()
	{
		return this.roleId;
	}

	public void setRoleId(Integer roleId)
	{
		this.roleId = roleId;
	}

	@Column(name = "RoleName", nullable = false, length = 512)
	@NotNull
	@Length(max = 512)
	public String getRoleName()
	{
		return this.roleName;
	}

	public void setRoleName(String roleName)
	{
		this.roleName = roleName;
	}

	// @Column(name = "Description", length = 512)
	@Column(name = "Description", columnDefinition = "TEXT")
	@Length(max = 512)
	public String getDescription()
	{
		return this.description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "role", cascade = CascadeType.ALL, orphanRemoval = true)
	public Set<UserRole> getUserRoles()
	{
		return this.userRoles;
	}

	public void setUserRoles(Set<UserRole> userRoles)
	{
		this.userRoles = userRoles;
	}

	@OneToMany(fetch = FetchType.EAGER, mappedBy = "secondaryRole", cascade = CascadeType.ALL, orphanRemoval = true)
	public Set<RoleToRole> getChildrenRoleToRole()
	{
		return childrenRoleToRole;
	}

	public void setChildrenRoleToRole(final Set<RoleToRole> childrenRoleToRole)
	{
		this.childrenRoleToRole = childrenRoleToRole;
	}

	@OneToMany(fetch = FetchType.EAGER, mappedBy = "primaryRole", cascade = CascadeType.ALL, orphanRemoval = true)
	public Set<RoleToRole> getParentRoleToRole()
	{
		return parentRoleToRole;
	}

	public void setParentRoleToRole(final Set<RoleToRole> parentRoleToRole)
	{
		this.parentRoleToRole = parentRoleToRole;
	}
	
	public boolean hasUser(final User user)
	{
		return hasUser(user.getUserId());
	}
	
	public boolean hasUser(final Integer user)
	{
		for (final UserRole userRole : this.userRoles)
		{
			if (userRole.getUser().getUserId().equals(user))
				return true;
		}

		return false;
	}

	public void addUser(final User user)
	{
		if (!hasUser(user))
		{
			final UserRole userRole = new UserRole(user, this);
			this.getUserRoles().add(userRole);
			user.getUserRoles().add(userRole);
		}
	}

	public void removeUser(final User user)
	{
		removeUser(user.getUserId());
	}

	public void removeUser(final Integer userId)
	{
		for (final UserRole userRole : this.userRoles)
		{
			if (userRole.getUser().getUserId().equals(userId))
			{
				this.getUserRoles().remove(userRole);
				userRole.getUser().getUserRoles().remove(userRole);
				break;
			}
		}
	}
	
	@Transient
	public String getUsersCommaSeperatedList()
	{
		String retValue = "";
		for (final UserRole role : this.userRoles)
		{
			if (retValue.length() != 0)
				retValue += ", ";
			retValue += role.getUser().getUserName();			
		}
		return retValue;
	}

}
