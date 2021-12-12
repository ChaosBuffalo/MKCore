package com.chaosbuffalo.mkcore.client.gui.widgets;

import com.chaosbuffalo.mkcore.client.gui.ParticleEditorScreen;
import com.chaosbuffalo.mkcore.fx.particles.animation_tracks.ParticleAnimationTrack;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.OffsetConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKLayout;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutVertical;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKButton;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKRectangle;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKText;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class AnimationTrackPanel extends MKStackLayoutVertical {

    private ParticleAnimationTrack track;
    private ParticleAnimationTrack.AnimationTrackType trackType;
    private final FontRenderer font;
    private final ParticleEditorScreen particleEditor;

    public AnimationTrackPanel(int x, int y, int width, ParticleAnimationTrack.AnimationTrackType trackType,
                               FontRenderer font, ParticleEditorScreen particleEditor) {
        super(x, y, width);
        this.track = null;
        this.trackType = trackType;
        this.font = font;
        this.particleEditor = particleEditor;
        setupLayout();
    }

    public void setTrack(ParticleAnimationTrack track) {
        this.track = track;
        setupLayout();
    }

    protected void setupLayout(){
        clearWidgets();
        MKLayout header = getHeader();
        addWidget(getHeader());
        MKRectangle divider = new MKRectangle(getX(), getY(), getWidth(), 1, 0xffffffff);
        addWidget(divider);
        if (track != null){
            SerializableAttributeContainerPanel panel = new SerializableAttributeContainerPanel(
                    0, 0, getWidth(), track, font, (attr) -> particleEditor.markDirty());
            addWidget(panel);
            MKRectangle divider2 = new MKRectangle(getX(), getY(), getWidth(), 1, 0xffffffff);
            addWidget(divider2);
        }

    }

    protected MKLayout getHeader(){
        MKStackLayoutVertical headerLayout = new MKStackLayoutVertical(0, 0, getWidth());
        headerLayout.setPaddings(2, 2, 2, 2);
        headerLayout.setMargins(2, 2,2, 2);
        ITextComponent trackName = getTrackName();
        MKText tracktextName = new MKText(font, trackName);
        tracktextName.setWidth(getWidth());
        tracktextName.setColor(0xffffffff);
        headerLayout.addWidget(tracktextName);
        if (track == null){
            MKButton setTrack = new MKButton(0, 0, 75, 20, new TranslationTextComponent("mkcore.particle_editor.add_track"));
            setTrack.setPressedCallback((button, click) -> particleEditor.promptAddTrack(trackType));
            headerLayout.addWidget(setTrack);
            headerLayout.addConstraintToWidget(new OffsetConstraint(10, 0, true, false), setTrack);
        } else {
            MKButton deleteTrack = new MKButton(0, 0, 75, 20, new TranslationTextComponent("mkcore.particle_editor.delete_track"));
            deleteTrack.setPressedCallback((button, click) -> particleEditor.deleteTrackButton(trackType));
            headerLayout.addWidget(deleteTrack);
            headerLayout.addConstraintToWidget(new OffsetConstraint(10, 0, true, false), deleteTrack);
        }
        return headerLayout;
    }

    ITextComponent getTrackName(){
        String trackName = track == null ? "Empty" : track.getTypeName().toString();
        switch (trackType){
            case COLOR:
                return new TranslationTextComponent("mkcore.particle_editor.track_type.color", trackName);
            case SCALE:
                return new TranslationTextComponent("mkcore.particle_editor.track_type.scale", trackName);
            case MOTION:
                return new TranslationTextComponent("mkcore.particle_editor.track_type.motion", trackName);
            case UNKNOWN:
            default:
                return new TranslationTextComponent("mkcore.particle_editor.track_type.unknown", trackName);
        }
    }


}
