package xyz.anythings.sec.entity;

import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Index;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.Table;
import xyz.elidom.orm.OrmConstants;
import xyz.elidom.orm.entity.basic.ElidomStampHook;

@Table(name = "play_groups_boards", idStrategy = GenerationRule.UUID, uniqueFields = "play_groups_id,boards_id", indexes = {
    @Index(name = "ix_play_groups_boards_0", columnList = "play_groups_id"),
    @Index(name = "ix_play_groups_boards_1", columnList = "boards_id")
})
public class PlayGroupBoard extends ElidomStampHook {
    /**
     * SerialVersion UID
     */
    private static final long serialVersionUID = 126169872239210125L;

    @PrimaryKey
    @Column(name = "play_groups_id", nullable = false, length = OrmConstants.FIELD_SIZE_UUID)
    private String playGroupsId;

    @PrimaryKey
    @Column(name = "boards_id", nullable = false, length = OrmConstants.FIELD_SIZE_UUID)
    private String boardsId;

	public String getPlayGroupsId() {
		return playGroupsId;
	}

	public void setPlayGroupsId(String playGroupsId) {
		this.playGroupsId = playGroupsId;
	}

	public String getBoardsId() {
		return boardsId;
	}

	public void setBoardsId(String boardsId) {
		this.boardsId = boardsId;
	}

}
