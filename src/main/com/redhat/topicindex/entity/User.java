package com.redhat.topicindex.entity;

// Generated Aug 8, 2011 9:22:31 AM by Hibernate Tools 3.4.0.CR1

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import static javax.persistence.GenerationType.IDENTITY;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.envers.Audited;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

/**
 * User generated by hbm2java
 */
@Entity
@Audited
@Table(name = "User", uniqueConstraints = @UniqueConstraint(columnNames =
{ "UserName" }))
public class User implements java.io.Serializable
{

	private static final long serialVersionUID = -1745432150593137619L;
	public static final String SELECT_ALL_QUERY = "select user from User user";
	private Integer userId;
	private String userName;
	private String description;
	private Set<UserRole> userRoles = new HashSet<UserRole>(0);

	public User()
	{
	}

	public User(String userName)
	{
		this.userName = userName;
	}

	public User(String userName, String description, Set<UserRole> userRoles)
	{
		this.userName = userName;
		this.description = description;
		this.userRoles = userRoles;
	}

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "UserID", unique = true, nullable = false)
	public Integer getUserId()
	{
		return this.userId;
	}

	public void setUserId(Integer userId)
	{
		this.userId = userId;
	}

	@Column(name = "UserName", nullable = false, length = 512)
	@NotNull
	@Length(max = 512)
	public String getUserName()
	{
		return this.userName;
	}

	public void setUserName(String userName)
	{
		this.userName = userName;
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

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	public Set<UserRole> getUserRoles()
	{
		return this.userRoles;
	}

	public void setUserRoles(final Set<UserRole> userRoles)
	{
		this.userRoles = userRoles;
	}

	@Transient
	public boolean isInRole(final Role role)
	{
		if (role == null)
			return false;

		return isInRole(role.getRoleId());
	}

	@Transient
	public boolean isInRole(final Integer role)
	{
		for (final UserRole userRole : this.userRoles)
		{
			if (userRole.getRole().getRoleId().equals(role))
				return true;
		}

		return false;
	}

	public void addRole(final Role role)
	{
		if (!isInRole(role))
		{
			final UserRole userRole = new UserRole(this, role);
			this.getUserRoles().add(userRole);
			role.getUserRoles().add(userRole);
		}
	}

	public void removeRole(final Role role)
	{
		removeRole(role.getRoleId());
	}

	public void removeRole(final Integer roleId)
	{
		for (final UserRole userRole : this.userRoles)
		{
			if (userRole.getRole().getRoleId().equals(roleId))
			{
				this.getUserRoles().remove(userRole);
				userRole.getRole().getUserRoles().remove(userRole);
				break;
			}
		}
	}
	
	@Transient
	public String getUserRolesCommaSeperatedList()
	{
		String retValue = "";
		for (final UserRole role : this.userRoles)
		{
			if (retValue.length() != 0)
				retValue += ", ";
			retValue += role.getRole().getRoleName();			
		}
		return retValue;
	}

}
