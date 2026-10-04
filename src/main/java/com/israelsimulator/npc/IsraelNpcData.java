package com.israelsimulator.npc;

import com.israelsimulator.npc.schedule.NpcSchedule;
import com.israelsimulator.npc.schedule.ScheduleState;
import java.util.UUID;
import net.minecraft.core.BlockPos;

/**
 * State and persistent attributes for an Israel-Simulator NPC (GAME_DESIGN.md §26, §27, §29).
 */
public class IsraelNpcData {

    private final UUID npcId;
    private NpcProfession profession;
    private BlockPos homePos;
    private BlockPos workPos;
    private BlockPos socialPos;
    private int reputation;
    private long lastTradeTick;
    private int dailyTradesExecuted;
    private String customName;

    public IsraelNpcData(UUID npcId, NpcProfession profession, BlockPos homePos, BlockPos workPos, BlockPos socialPos) {
        this.npcId = npcId;
        this.profession = profession;
        this.homePos = homePos != null ? homePos : BlockPos.ZERO;
        this.workPos = workPos != null ? workPos : BlockPos.ZERO;
        this.socialPos = socialPos != null ? socialPos : BlockPos.ZERO;
        this.reputation = 0;
        this.lastTradeTick = 0L;
        this.dailyTradesExecuted = 0;
        this.customName = profession.getDisplayName();
    }

    public UUID getNpcId() {
        return npcId;
    }

    public NpcProfession getProfession() {
        return profession;
    }

    public void setProfession(NpcProfession profession) {
        this.profession = profession;
    }

    public BlockPos getHomePos() {
        return homePos;
    }

    public void setHomePos(BlockPos homePos) {
        this.homePos = homePos;
    }

    public BlockPos getWorkPos() {
        return workPos;
    }

    public void setWorkPos(BlockPos workPos) {
        this.workPos = workPos;
    }

    public BlockPos getSocialPos() {
        return socialPos;
    }

    public void setSocialPos(BlockPos socialPos) {
        this.socialPos = socialPos;
    }

    public int getReputation() {
        return reputation;
    }

    public void adjustReputation(int delta) {
        this.reputation = Math.clamp(this.reputation + delta, -100, 100);
    }

    public long getLastTradeTick() {
        return lastTradeTick;
    }

    public void recordTrade(long gameTime) {
        if ((gameTime - this.lastTradeTick) > 24000L) {
            this.dailyTradesExecuted = 0; // Reset daily limit on new day
        }
        this.lastTradeTick = gameTime;
        this.dailyTradesExecuted++;
    }

    public int getDailyTradesExecuted() {
        return dailyTradesExecuted;
    }

    public boolean canTradeToday() {
        return dailyTradesExecuted < 16; // Anti-exploit: max 16 trades per day per NPC
    }

    public String getCustomName() {
        return customName;
    }

    public void setCustomName(String customName) {
        this.customName = customName;
    }

    /**
     * Determines which location this NPC should target right now based on their schedule.
     */
    public BlockPos getTargetLocation(long dayTime, boolean isShabbat) {
        ScheduleState state = NpcSchedule.getScheduleState(dayTime, isShabbat, profession);
        return switch (state) {
            case WORK_MORNING, WORK_AFTERNOON -> workPos;
            case LUNCH, SOCIAL -> socialPos;
            case WAKE, HOME, SLEEP, SHABBAT_REST -> homePos;
            case SHABBAT_PRAYER -> workPos; // Or synagogue
        };
    }
}
