/*
 * MutableCophylogeneticTrajectoryState.java
 *
 * Cophy: Cophylogenetics for BEAST
 *
 * Copyright (C) 2014 Arman Bilge <armanbilge@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package cophy.simulation;

import cophy.model.CophylogenyModel;
import dr.evolution.tree.NodeRef;
import dr.evolution.tree.Tree;

import java.util.Map;

/**
 *
 * @author Arman D. Bilge <armanbilge@gmail.com>
 *
 */
public interface MutableCophylogeneticTrajectoryState extends CophylogeneticTrajectoryState {

    void reset(Tree guest, CophylogenyModel model);

    void setHeight(double height);

    void incrementGuestCountAtHost(NodeRef guest, NodeRef host);

    void decrementGuestCountAtHost(NodeRef guest, NodeRef host);

    void setGuestCountAtHost(NodeRef guest, NodeRef host, int guestCount);

    void setGuestCountsAtHost(NodeRef host, Map<NodeRef,Integer> guestCounts);

    void addGuest(NodeRef guest);

    void removeGuest(NodeRef guest);

    void addHost(NodeRef host);

    void removeHost(NodeRef host);

    void applyEvent(CophylogeneticEvent event);

    void addListener(MutableCophylogeneticTrajectoryStateListener listener);

    void removeListener(MutableCophylogeneticTrajectoryStateListener listener);

    public interface MutableCophylogeneticTrajectoryStateListener {

        void stateChangedEvent(MutableCophylogeneticTrajectoryState state, CophylogeneticEvent event);

    }

    public class InvalidCophylogeneticTrajectoryStateException extends RuntimeException {

        public InvalidCophylogeneticTrajectoryStateException(final String message) {
            super(message);
        }

    }

}
