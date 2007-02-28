package ejava.examples.orm.onetomany.annotated;

import java.util.ArrayList;
import java.util.Collection;

import javax.persistence.*;

/**
 * This class implements a parent in a OneToMany, uni-directional relationship
 * test case. We are looking to see where the foreign key gets placed; in 
 * a link/join table or the child side of the relationship. The child class
 * knows nothing of this relationship.<p/>
 * 
 * This is the schema when nothing besides @OneToMany is defined<p/>
 * <pre>
    @OneToMany
    public Collection<OneManyChild> getChildren() {
        return children;
    }
    </pre><p/>
 * <pre>
    create table ORMO2M_CHILD (
        CHILDID bigint generated by default as identity (start with 1),
        name varchar(255),
        primary key (CHILDID)
    );

    create table ORMO2M_PARENT (
        PARENTID bigint generated by default as identity (start with 1),
        name varchar(255),
        primary key (PARENTID)
    );

    create table ORMO2M_PARENT_ORMO2M_CHILD (
        ORMO2M_PARENT_PARENTID bigint not null,
        children_CHILDID bigint not null,
        unique (children_CHILDID)
    );
    alter table ORMO2M_PARENT_ORMO2M_CHILD
        add constraint FK9732C673B120B8B4
        foreign key (children_CHILDID)
        references ORMO2M_CHILD;

    alter table ORMO2M_PARENT_ORMO2M_CHILD
        add constraint FK9732C673C25EC1A0
        foreign key (ORMO2M_PARENT_PARENTID)
        references ORMO2M_PARENT;

    </pre><p/>
 *  
 *  Adding @JoinColumn(name="PARENT_ID") to the parent side...
 * <pre>
    @OneToMany
    @JoinColumn(name="PARENT_ID")
    public Collection<OneManyChild> getChildren() {
        return children;
    }
    </pre><p/>
 * <pre>
    create table ORMO2M_CHILD (
        CHILDID bigint generated by default as identity (start with 1),
        name varchar(255),
        PARENT_ID bigint,
        primary key (CHILDID)
    );

    create table ORMO2M_PARENT (
        PARENTID bigint generated by default as identity (start with 1),
        name varchar(255),
        primary key (PARENTID)
    );
    alter table ORMO2M_CHILD
        add constraint FK257187DDF37CA975
        foreign key (PARENT_ID)
        references ORMO2M_PARENT;
    </pre><p/>
 *  
 *  Adding @JoinTable/@JoinColumn(name="PARENT_ID") to the parent side...
 * <pre>
    @OneToMany
    @JoinTable(
            joinColumns=@JoinColumn(name="PARENT_ID"))
    public Collection<OneManyChild> getChildren() {
        return children;
    }
    </pre><p/>
 * <pre>
    create table ORMO2M_CHILD (
        CHILDID bigint generated by default as identity (start with 1),
        name varchar(255),
        primary key (CHILDID)
    );

    create table ORMO2M_PARENT (
        PARENTID bigint generated by default as identity (start with 1),
        name varchar(255),
        primary key (PARENTID)
    );

    create table ORMO2M_PARENT_ORMO2M_CHILD (
        PARENT_ID bigint not null,
        children_CHILDID bigint not null,
        unique (children_CHILDID)
    );
    alter table ORMO2M_PARENT_ORMO2M_CHILD
        add constraint FK9732C673B120B8B4
        foreign key (children_CHILDID)
        references ORMO2M_CHILD;

    alter table ORMO2M_PARENT_ORMO2M_CHILD
        add constraint FK9732C673F37CA975
        foreign key (PARENT_ID)
        references ORMO2M_PARENT;
    </pre><p/>
 *  
 *  
 * @author jcstaff
 *
 */
@Entity(name="O2MOwningParent") @Table(name="ORMO2M_PARENT")
public class OneManyOwningParent {
    private long id;
    private String name;
    private Collection<OneManyChild> children = new ArrayList<OneManyChild>();
    
    public OneManyOwningParent() {}
    public OneManyOwningParent(long id) {
        this.id = id;
    }
    public OneManyOwningParent(String name) {
        this.name = name;
    }
    
    @Id @GeneratedValue @Column(name="PARENTID")
    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    @OneToMany
    @JoinColumn(name="PARENT_ID")
    //@JoinTable(
    //        joinColumns=@JoinColumn(name="PARENT_ID"))
    public Collection<OneManyChild> getChildren() {
        return children;
    }
    public void setChildren(Collection<OneManyChild> children) {
        this.children = children;
    }

    public String toString() {
        StringBuilder text = new StringBuilder();
        text.append("id=" + id);
        text.append(", name=" + name);
        text.append(", children=(" + children.size() + ")" + children);
        return text.toString();
    }
}
