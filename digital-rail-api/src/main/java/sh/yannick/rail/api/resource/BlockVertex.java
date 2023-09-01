package sh.yannick.rail.api.resource;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import sh.yannick.math.Vertex;
import sh.yannick.state.Resource;

@RequiredArgsConstructor
public class BlockVertex implements Vertex {
    @Getter
    private final Resource<Block.Spec, Block.Status> block;

    @Override
    public String getLabel() {
        return block.getMetadata().getName();
    }

    @Override
    public String toString() {
        return getLabel();
    }
}
