package dragon.service.sec;

import java.io.Serializable;
import java.security.Principal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Identity implements Principal, Serializable, Cloneable {
    private static final long serialVersionUID = -1339115850583584835L;

    private String fullName;
    private String custName;
    private String custFullName;
    private String subject;
    private String naturalId;
    private String description;
    private long id;
    private long custId;
    private transient boolean sticky;
    private transient String custKey;
    private boolean svcIdent; // make it persistable
    private transient String clientAddr;
    private Long primaryRoleId;
    private Set<Long> orgIds;	// IDs of the orgs that the user can access, empty or null to access all orgs
    private Map<Long, Long> orgIdToRoleIdMap;
    private Long collectorId;
    private String authenProfiles2;
    private String uDomain;

    public String getNaturalId() {
        return naturalId;
    }

    public void setNaturalId(String naturalId) {
        this.naturalId = naturalId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getCustId() {
        return custId;
    }

    public void setCustId(long custId) {
        this.custId = custId;
    }

    public String getCustName() {
        return custName;
    }

    public void setCustName(String custName) {
        this.custName = custName;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean isSticky() {
        return sticky;
    }

    public void setSticky(boolean sticky) {
        this.sticky = sticky;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getName() {
        return custName + "/" + subject;
    }

    public boolean isSuperUser() {
        return custId == 1;
    }
    
    public boolean isSystemUser() {
        return custId == 0;
    }

    public String getCustKey() {
        return custKey;
    }

    public void setCustKey(String param) {
        this.custKey = param;
    }

    public Long getPrimaryRoleId() {
        return primaryRoleId;
    }

    public void setPrimaryRoleId(Long primaryRoleId) {
        this.primaryRoleId = primaryRoleId;
    }

    public Set<Long> getOrgIds() {
	return orgIds;
    }
    
    public void setOrgIds(Set<Long> orgIds) {
	this.orgIds = orgIds;
    }
    
    public void addOrgToRoleMap(Long orgId, Long roleId) {
	if (orgIdToRoleIdMap == null) {
	    orgIdToRoleIdMap = new HashMap<Long, Long>();
	}
	orgIdToRoleIdMap.put(orgId, roleId);
    }
    
    public Long getRoleIdOfOrg(Long orgId) {
	return (orgIdToRoleIdMap == null ? null : orgIdToRoleIdMap.get(orgId));
    }
    
    public Map<Long, Long> getOrgIdToRoleIdMap() {
	return orgIdToRoleIdMap;
    }
    
    public void setOrgIdToRoleIdMap(Map<Long, Long> orgIdToRoleIdMap) {
	this.orgIdToRoleIdMap = orgIdToRoleIdMap;
    }
    
    public String getCustFullName() {
        return custFullName;
    }

    public void setCustFullName(String custFullName) {
        this.custFullName = custFullName;
    }

    public boolean isSvcIdent() {
        return svcIdent;
    }

    public void setSvcIdent(boolean svcIdent) {
        this.svcIdent = svcIdent;
    }

    public String getClientAddr() {
        return clientAddr;
    }

    public void setClientAddr(String clientAddr) {
        this.clientAddr = clientAddr;
    }

    public Long getCollectorId() {
        return collectorId;
    }

    public void setCollectorId(Long collectorId) {
        this.collectorId = collectorId;
    }

    public String getAuthenProfiles2() {
        return authenProfiles2;
    }

    public void setAuthenProfiles2(String authenProfiles2) {
        this.authenProfiles2 = authenProfiles2;
    }

    public String getuDomain() {
        return uDomain;
    }

    public void setuDomain(String uDomain) {
        this.uDomain = uDomain;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Identity other = (Identity) obj;
        if ((this.custName == null) ? (other.custName != null) : !this.custName.equals(other.custName)) {
            return false;
        }
        if ((this.collectorId == null) ? (other.collectorId != null) : !this.collectorId.equals(other.collectorId)) {
            return false;
        }
        if ((this.subject == null) ? (other.subject != null) : !this.subject.equals(other.subject)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 53 * hash + (this.custName != null ? this.custName.hashCode() : 0);
        hash = 53 * hash + (this.subject != null ? this.subject.hashCode() : 0);
        return hash;
    }

    @Override
    protected Identity clone() {
        try {
            Identity si = (Identity) super.clone();
            si.setCustId(custId);
            si.setCustName(custName);
            si.setDescription(description);
            si.setFullName(fullName);
            si.setId(id);
            si.setNaturalId(naturalId);
            si.setSticky(sticky);
            si.setSubject(subject);
            si.setCustKey(custKey);
            si.setSvcIdent(svcIdent);
            si.setClientAddr(clientAddr);
            si.setAuthenProfiles2(authenProfiles2);
            si.setuDomain(uDomain);
            if (primaryRoleId != null) {
                si.primaryRoleId = primaryRoleId;
            }
            if (orgIds != null) {
            	si.orgIds = new HashSet<Long>(orgIds.size());
            	si.orgIds.addAll(orgIds);
            }
            if (orgIdToRoleIdMap != null) {
            	si.orgIdToRoleIdMap = new HashMap<Long, Long>(orgIdToRoleIdMap.size());
            	si.orgIdToRoleIdMap.putAll(orgIdToRoleIdMap);
            }
            return si;
        } catch (CloneNotSupportedException ex) {
            throw new RuntimeException(ex);
        }
    }
}
