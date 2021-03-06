package ejava.examples.orm.map.annotated;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.*;

/**
 * This class represents the owning side of a Many-to-Many, bi-directional and 
 * uni-directional relationship. The ManyManyEntity
 * relationship is uni-directional from this entity. The ManyManyInverseEntity
 * relationship is bi-directional, but owned by this entity.
 * @author jcstaff
 *
 */
@Entity @Table(name="ORMMAP_MANYMANY_OWNENTITY")
public class ManyManyOwningEntity {
	private String name;
	private Map<String, ManyManyEntity> ownedEntities =
		new HashMap<String, ManyManyEntity>();
	private Map<String, ManyManyInverseEntity> ownedInverseEntities =
		new HashMap<String, ManyManyInverseEntity>();	
	
	protected ManyManyOwningEntity() {}
	public ManyManyOwningEntity(String name) {
		this.name = name;
	}
	
	@Id
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	@ManyToMany
    @MapKey(name="name")
	public Map<String, ManyManyEntity> getOwnedEntities() {
		return ownedEntities;
	}
	public void setOwnedEntities(Map<String, ManyManyEntity> ownedEntities) {
		this.ownedEntities = ownedEntities;
	}
	
	@ManyToMany
    @MapKey(name="name")
	public Map<String, ManyManyInverseEntity> getOwnedInverseEntities() {
		return ownedInverseEntities;
	}
	public void setOwnedInverseEntities(
			Map<String, ManyManyInverseEntity> ownedInverseEntities) {
		this.ownedInverseEntities = ownedInverseEntities;
	}
	
	public String toString() {
		StringBuilder text = new StringBuilder();
		text.append(getClass().getName());
		text.append(", name=" + name);
		if (ownedEntities != null) {
			text.append(", ownedEntities=" + ownedEntities);
		}
		if (ownedInverseEntities != null) {
			text.append(", ownedInverseEntities=" + ownedInverseEntities);
		}
		
		return text.toString();
	}

}
