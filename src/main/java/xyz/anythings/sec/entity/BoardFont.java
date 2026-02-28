package xyz.anythings.sec.entity;

import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Index;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.Table;
import xyz.elidom.orm.OrmConstants;
import xyz.elidom.orm.entity.basic.ElidomStampHook;

@Table(name = "fonts", idStrategy = GenerationRule.UUID, uniqueFields = "domainId,name", indexes = {
    @Index(name = "ix_fonts_0", columnList = "domain_id,name", unique = true)
})
public class BoardFont extends ElidomStampHook {
    /**
     * SerialVersion UID
     */
    private static final long serialVersionUID = 126169872239210999L;

    @PrimaryKey
    @Column(name = "id", nullable = false, length = OrmConstants.FIELD_SIZE_UUID)
    private String id;

    @Column(name = "name", nullable = false, length = OrmConstants.FIELD_SIZE_NAME)
    private String name;

    @Column(name = "provider", length = 100)
    private String provider;

    @Column(name = "uri", length = 500)
    private String uri;

    @Column(name = "path", length = 500)
    private String path;
    
    @Column(name = "active")
    private Boolean active;

    public BoardFont() {
    }

    public BoardFont(String id) {
        this.id = id;
    }

    public BoardFont(Long domainId, String name) {
        this.domainId = domainId;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
