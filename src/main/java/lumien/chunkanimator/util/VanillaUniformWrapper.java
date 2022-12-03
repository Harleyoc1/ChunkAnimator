package lumien.chunkanimator.util;

import com.mojang.blaze3d.shaders.Uniform;

public class VanillaUniformWrapper implements UniformWrapper<Uniform> {

    private final Uniform uniform;

    public VanillaUniformWrapper(Uniform uniform) {
        this.uniform = uniform;
    }

    @Override
    public void set(float x, float y, float z) {
        uniform.set(x, y, z);
    }

}
