package sh.yannick.rail.interlocking.listener;

import sh.yannick.rail.api.resource.Block;
import sh.yannick.state.Listener;
import sh.yannick.state.ResourceListener;

@Listener(apiVersion = "rail.yannick.sh/v1alpha1", kind = "Block")
public class BlockListener implements ResourceListener<Block.Spec, Block.Status, Block> {
    @Override
    public void onCreate(Block block) {
        if (!block.getSpec().getStopPoints().isEmpty() && block.getSpec().getStopPoints().size() > 2) {
            throw new IllegalArgumentException("Block " + block.getMetadata().getName() + " has more than 2 stop points. This makes no sense.");
        }

        // TODO: This is the place where we need to check if the block is still occupied
        if (block.getStatus() == null) {
            block.setStatus(new Block.Status());
        }
        block.getStatus().setLocked(block.getSpec().isLocked());
    }
}
