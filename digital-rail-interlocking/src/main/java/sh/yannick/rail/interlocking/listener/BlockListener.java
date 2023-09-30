package sh.yannick.rail.interlocking.listener;

import lombok.extern.slf4j.Slf4j;
import sh.yannick.rail.api.resource.Block;
import sh.yannick.state.Listener;
import sh.yannick.state.ResourceListener;

@Slf4j
@Listener(apiVersion = "rail.yannick.sh/v1alpha1", kind = "Block")
public class BlockListener implements ResourceListener<Block.Spec, Block.Status, Block> {
    @Override
    public void onCreate(Block block) {
        if (!block.getSpec().getStopPoints().isEmpty() && block.getSpec().getStopPoints().size() > 2) {
            throw new IllegalArgumentException("Block " + block.getMetadata().getName() + " has more than 2 stop points. This makes no sense.");
        }

        Block.Status status = new Block.Status();
        block.setStatus(status);

        status.setLocked(block.getSpec().isLocked());
        status.setOccupied(block.getSpec().isOccupied());
    }

    @Override
    public void onUpdate(Block resource) {
        if (resource.getSpec().isOccupied() && !resource.getStatus().isLocked()) {
            resource.getStatus().setOccupied(true);
            resource.addError("Block %s should be set to occupied although it is not locked.", resource.getMetadata().getName());
            log.warn("Block {} should be set to occupied although it is not locked.", resource.getMetadata().getName());
        }

        if (resource.getSpec().isOccupied() && resource.getStatus().isLocked()) {
            resource.getStatus().setOccupied(true);
            log.info("Block {} is occupied.", resource.getMetadata().getName());
        }

        if (!resource.getSpec().isOccupied() && !resource.getStatus().isLocked()) {
            resource.getStatus().setOccupied(false);
            log.warn("Block {} is not occupied anymore (it has never been ...).", resource.getMetadata().getName());
        }

        if (!resource.getSpec().isOccupied() && resource.getStatus().isLocked()) {
            resource.getStatus().setOccupied(false);
            resource.getStatus().setLocked(false);
            log.info("Block {} is released.", resource.getMetadata().getName());
        }

        if (resource.getSpec().isLocked() && !resource.getStatus().isLocked()) {
            resource.getStatus().setLocked(true);
            log.info("Block {} is locked.", resource.getMetadata().getName());
        }

        if (!resource.getSpec().isLocked() && resource.getStatus().isOccupied()) {
            resource.addError("Block %s should be released but is still occupied.", resource.getMetadata().getName());
            log.warn("Block {} should be released but is still occupied.", resource.getMetadata().getName());
        }
    }
}
