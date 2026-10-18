package com.israelsimulator.mixin;

import com.israelsimulator.world.structure.StructureOverlapGuard;
import java.util.Optional;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Cancels a mod structure start that would overlap a higher-priority mod structure (see the guard). */
@Mixin(Structure.class)
public abstract class StructureOverlapGuardMixin {

    @Inject(method = "findValidGenerationPoint", at = @At("RETURN"), cancellable = true)
    private void israelSimulator$noOverlap(Structure.GenerationContext context,
                                           CallbackInfoReturnable<Optional<Structure.GenerationStub>> cir) {
        if (cir.getReturnValue().isEmpty() || StructureOverlapGuard.bypassed()) {
            return;
        }
        if (!StructureOverlapGuard.allows((Structure) (Object) this, context)) {
            cir.setReturnValue(Optional.empty());
        }
    }
}
