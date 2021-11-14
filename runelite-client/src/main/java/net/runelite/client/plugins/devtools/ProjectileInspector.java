package net.runelite.client.plugins.devtools;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ProjectileMoved;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.DynamicGridLayout;
import net.runelite.client.ui.FontManager;
import net.runelite.client.util.ImageUtil;
import org.apache.commons.lang3.tuple.Pair;

import javax.inject.Inject;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.util.*;
import java.util.List;

/**
 * @author Kris | 21/10/2021
 */
@SuppressWarnings("DuplicatedCode")
public class ProjectileInspector extends DevToolsFrame {

    private final static int MAX_LOG_ENTRIES = 10_000;
    private final Map<ProjectileTracker.EarlyProjectileInfo, List<Pair<ProjectileTracker.DynamicProjectileInfo, Pair<JLabel, JButton>>>> trackedProjectiles = Collections.synchronizedMap(new HashMap<>());
    private final Client client;
    private final EventBus eventBus;
    private final ProjectileTracker projectileTracker;
    private final ImageIcon completeIcon = new ImageIcon(ImageUtil.loadImageResource(getClass(), "complete_copy.png"));
    private final ImageIcon incompleteIcon = new ImageIcon(ImageUtil.loadImageResource(getClass(), "incomplete_copy.png"));
    private final JPanel tracker = new JPanel();
    private final JCheckBox showPlayers = new JCheckBox("Show from players", true);
    private final JCheckBox showNpcs = new JCheckBox("Show from NPCs", true);
    private final JCheckBox showUnknowns = new JCheckBox("Show from unknown", true);
    private final String spacing = "   ";

    private int lastTick = 0;

    @Inject
    ProjectileInspector(Client client, EventBus eventBus, ProjectileTracker projectileTracker) {
        this.client = client;
        this.eventBus = eventBus;
        this.projectileTracker = projectileTracker;
        setTitle("Projectile Inspector");
        setLayout(new BorderLayout());
        tracker.setLayout(new DynamicGridLayout(0, 1, 0, 3));
        final JPanel trackerWrapper = new JPanel();
        trackerWrapper.setLayout(new BorderLayout());
        trackerWrapper.add(tracker, BorderLayout.NORTH);

        final JScrollPane trackerScroller = new JScrollPane(trackerWrapper);
        trackerScroller.setPreferredSize(new Dimension(1200, 400));

        final JScrollBar vertical = trackerScroller.getVerticalScrollBar();
        vertical.addAdjustmentListener(new AdjustmentListener() {
            int lastMaximum = actualMax();

            private int actualMax()
            {
                return vertical.getMaximum() - vertical.getModel().getExtent();
            }

            @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
                if (vertical.getValue() >= lastMaximum) {
                    vertical.setValue(actualMax());
                }
                lastMaximum = actualMax();
            }
        });

        add(trackerScroller, BorderLayout.CENTER);


        final JPanel trackerOpts = new JPanel();
        trackerOpts.setLayout(new FlowLayout());

        trackerOpts.add(showPlayers);
        trackerOpts.add(showNpcs);
        trackerOpts.add(showUnknowns);

        final JButton clearBtn = new JButton("Clear");
        clearBtn.addActionListener(e -> {
            tracker.removeAll();
            tracker.revalidate();
        });
        trackerOpts.add(clearBtn);

        final JButton clearCacheBtn = new JButton("Clear cache");
        clearCacheBtn.addActionListener(e -> {
            trackedProjectiles.clear();
            projectileTracker.clearCache();
            tracker.removeAll();
            tracker.revalidate();
        });
        trackerOpts.add(clearCacheBtn);
        add(trackerOpts, BorderLayout.SOUTH);

        tracker.add(new JLabel("The projectile inspector will require each unique projectile to be received from two different distances" +
                " in order for it to be able to identify all of the projectile parameters."));
        tracker.add(new JLabel("This is due to one of the properties of projectile being the equivalent of (lengthAdjustment + (chebyshevDistance * " +
                "stepMultiplier))."));
        tracker.add(new JLabel("Warning: Large npcs (size >= 3) cannot accurately be determined through this plugin as they are done on a per-script basis."));

        pack();
    }

    private void addEntry(ProjectileTracker.EarlyProjectileInfo earlyProjectile,
                          ProjectileTracker.DynamicProjectileInfo dynamicInfo,
                          String prefix,
                          String text) {
        if (prefix.startsWith("Player") && !showPlayers.isSelected()) return;
        if (prefix.startsWith("Npc") && !showNpcs.isSelected()) return;
        if (prefix.startsWith("Unknown") && !showUnknowns.isSelected()) return;
        int tick = client.getTickCount();
        SwingUtilities.invokeLater(() -> {
            if (tick != lastTick) {
                lastTick = tick;
                JLabel header = new JLabel("Tick " + tick);
                header.setFont(FontManager.getRunescapeSmallFont());
                header.setBorder(new CompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, ColorScheme.LIGHT_GRAY_COLOR),
                        BorderFactory.createEmptyBorder(3, 6, 0, 0)
                ));
                tracker.add(header);
            }

            if (text.startsWith("Incomplete")) {
                List<Pair<ProjectileTracker.DynamicProjectileInfo, Pair<JLabel, JButton>>> existingDynamicInfos
                        = trackedProjectiles.computeIfAbsent(earlyProjectile, k -> new ArrayList<>());
                JLabel jLabel = new JLabel(prefix + spacing + text);
                jLabel.setLayout(new BorderLayout());
                existingDynamicInfos.add(Pair.of(dynamicInfo, Pair.of(jLabel, addJButton(jLabel, true))));
                tracker.add(jLabel);
            } else {
                JLabel jLabel = new JLabel(prefix + spacing + text);
                jLabel.setLayout(new BorderLayout());
                addJButton(jLabel, false);
                tracker.add(jLabel);
                List<Pair<ProjectileTracker.DynamicProjectileInfo, Pair<JLabel, JButton>>> existingDynamicInfos = trackedProjectiles.get(earlyProjectile);
                /* If there are previous incomplete versions of this projectile, update their states to complete. */
                if (existingDynamicInfos != null) {
                    existingDynamicInfos.forEach(dynamicProjectileInfoJLabelPair -> {
                        Pair<JLabel, JButton> existingJPair = dynamicProjectileInfoJLabelPair.getRight();
                        JLabel existingLabel = existingJPair.getLeft();
                        JButton existingButton = existingJPair.getRight();
                        existingButton.setIcon(completeIcon);
                        existingLabel.setText(existingLabel.getText().substring(0, existingLabel.getText().indexOf(spacing))
                                + spacing + projectileTracker.getProjectileText(earlyProjectile, dynamicInfo));
                        updateJButtonListener(existingButton, existingLabel);
                    });
                    trackedProjectiles.remove(earlyProjectile);
                }
            }

            // Cull very old stuff
            while (tracker.getComponentCount() > MAX_LOG_ENTRIES)
            {
                tracker.remove(0);
            }

            tracker.revalidate();
        });
    }

    private JButton addJButton(JLabel label, boolean incomplete) {
        final JButton button = new JButton(incomplete ? incompleteIcon : completeIcon);
        button.setFocusPainted(false);
        updateJButtonListener(button, label);
        label.add(button, BorderLayout.EAST);
        return button;
    }

    private void updateJButtonListener(JButton button, JLabel label) {
        String textToCopy = label.getText().substring(label.getText().indexOf("Projectile("));
        Arrays.stream(button.getActionListeners()).forEach(button::removeActionListener);
        button.addActionListener(e -> {
            StringSelection stringSelection = new StringSelection(textToCopy);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection, null);
        });
    }

    @Subscribe
    public void onProjectileMoved(ProjectileMoved event) {
        projectileTracker.submitProjectileMoved(client, event, false, this::addEntry, false);
    }

    @Override
    public void open() {
        eventBus.register(this);
        super.open();
    }

    @Override
    public void close() {
        super.close();
        projectileTracker.clearCache();
        trackedProjectiles.clear();
        tracker.removeAll();
        eventBus.unregister(this);
    }
}
