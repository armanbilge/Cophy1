/**
 * CophylogeneticEvent.java
 *
 * Cophy: Cophylogenetics for BEAST
 *
 * Copyright (C) 2014 Arman D. Bilge <armanbilge@gmail.com>
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

import org.apache.commons.math.util.MathUtils;

import cophy.CophyUtils;
import cophy.model.Reconciliation;
import dr.evolution.tree.NodeRef;
import dr.evolution.tree.Tree;

/**
 *
 * @author Arman D. Bilge <armanbilge@gmail.com>
 *
 */
public abstract class CophylogeneticEvent {

    protected final String eventName;
    protected final double eventHeight;

    public CophylogeneticEvent(final String eventName,
                               final double eventHeight) {
        this.eventName = eventName;
        this.eventHeight = eventHeight;
    }

    public String getName() {
        return eventName;
    }

    public double getHeight() {
        return eventHeight;
    }


    public boolean isSpeciation() {
        return false;
    }

    protected abstract void
            apply(final MutableCophylogeneticTrajectoryState state)
            throws CophylogeneticEventFailedException;

    public static abstract class SpeciationEvent extends CophylogeneticEvent {

        public SpeciationEvent(String eventName, double eventHeight) {
            super(eventName, eventHeight);
        }

        @Override
        public final boolean isSpeciation() {
            return true;
        }

        public final double
                getProbabilityUnobserved(final
                                         CophylogeneticTrajectoryState state,
                                         final Tree guestTree,
                                         final Reconciliation reconciliation) {

            if (state.getHeight() != eventHeight)
                throw new RuntimeException("State incompatible with event.");

            return calculateProbabilityUnobserved(state,
                                                  guestTree,
                                                  reconciliation);

        }

        protected abstract double
                calculateProbabilityUnobserved(final
                                               CophylogeneticTrajectoryState
                                               state,
                                               final Tree guestTree,
                                               final
                                               Reconciliation reconciliation);

        public final double
                getProbabilityObserved(final
                                       CophylogeneticTrajectoryState state,
                                       final Tree guestTree,
                                       final Reconciliation reconciliation) {

            if (state.getHeight() != eventHeight)
                throw new RuntimeException("State incompatible with event.");

            return calculateProbabilityObserved(state,
                                                guestTree,
                                                reconciliation);

}

        protected abstract double
                calculateProbabilityObserved(final
                                             CophylogeneticTrajectoryState
                                             state,
                                             final Tree guestTree,
                                             final
                                             Reconciliation reconciliation);


    }

    public static class CospeciationEvent extends SpeciationEvent {

        private static final String COSPECIATION_EVENT = "cospeciationEvent";
        protected final Tree tree;
        protected final NodeRef node;

        public CospeciationEvent(final Tree tree,
                                 final NodeRef node,
                                 final double eventHeight) {
            super(COSPECIATION_EVENT, eventHeight);
            this.tree = tree;
            this.node = node;
        }

        @Override
        public void apply(final MutableCophylogeneticTrajectoryState state) {

            final int guestCount = state.getGuestCountAtHost(node);
            state.removeHost(node);
            final NodeRef left = tree.getChild(node, 0);
            final NodeRef right = tree.getChild(node, 1);
            state.addHost(left, guestCount);
            state.addHost(right, guestCount);

        }

        @Override
        protected double
                calculateProbabilityUnobserved(final
                                               CophylogeneticTrajectoryState
                                               state,
                                               final Tree guestTree,
                                               final
                                               Reconciliation reconciliation) {

            final int observedGuestCount = CophyUtils
                    .getGuestCountAtHostAtHeight(guestTree,
                                                 reconciliation,
                                                 node,
                                                 eventHeight);
            final int completeGuestCount = state.getGuestCountAtHost(node);
            // TODO Replace factorial division with a simplified form for
            // increased numerical stability
            final double invalidCombinations =
                    MathUtils.factorialDouble(observedGuestCount);
            final double totalCombinations =
                    MathUtils.factorialDouble(completeGuestCount);

            return (totalCombinations - invalidCombinations)
                    / totalCombinations;
        }

        @Override
        protected double
                calculateProbabilityObserved(final
                                             CophylogeneticTrajectoryState
                                             state,
                                             final Tree guestTree,
                                             final
                                             Reconciliation reconciliation) {

            final int completeGuestCount = state.getGuestCountAtHost(node);
            final double totalCombinations =
                    MathUtils.factorialDouble(completeGuestCount);

            return 1.0 / totalCombinations;
        }

    }

    public static abstract class BirthEvent extends SpeciationEvent {

        final protected NodeRef source;
        final protected NodeRef destination;

        public BirthEvent(final String eventName,
                          final double eventHeight,
                          final NodeRef source,
                          final NodeRef destination) {
            super(eventName, eventHeight);
            this.source = source;
            this.destination = destination;
        }

        public NodeRef getSource() {
            return source;
        }

        public NodeRef getDestination() {
            return destination;
        }

        @Override
        public void apply(final MutableCophylogeneticTrajectoryState state) {
            state.incrementGuestCountAtHost(destination);
        }

        @Override
        protected double
                calculateProbabilityUnobserved(final
                                               CophylogeneticTrajectoryState
                                               state,
                                               final Tree guestTree,
                                               final
                                               Reconciliation reconciliation) {

            final int completeGuestCountSource =
                    state.getGuestCountAtHost(source);
            final int observedGuestCountSource = CophyUtils
                        .getGuestCountAtHostAtHeight(guestTree,
                                                     reconciliation,
                                                     source,
                                                     eventHeight);

            final long totalCombinations;
            final long invalidCombinations;
            if (source.equals(destination)) {
                totalCombinations = CophyUtils
                        .extendedBinomialCoefficient(completeGuestCountSource,
                                                     2);
                invalidCombinations = CophyUtils
                        .extendedBinomialCoefficient(observedGuestCountSource,
                                                     2);
            } else {
                final int completeGuestCountDestination =
                        state.getGuestCountAtHost(destination);
                final int observedGuestCountDestination = CophyUtils
                        .getGuestCountAtHostAtHeight(guestTree,
                                                     reconciliation,
                                                     destination,
                                                     eventHeight);

                totalCombinations = completeGuestCountSource
                        * completeGuestCountDestination;
                invalidCombinations = observedGuestCountSource
                        * observedGuestCountDestination;

            }

            return (totalCombinations - invalidCombinations)
                    / (double) totalCombinations;
        }

        @Override
        protected double
                calculateProbabilityObserved(final
                                             CophylogeneticTrajectoryState
                                             state,
                                             final Tree guestTree,
                                             final
                                             Reconciliation reconciliation) {

            final int completeGuestCountSource =
                    state.getGuestCountAtHost(source);

            final long totalCombinations;
            if (source.equals(destination)) {
                totalCombinations = CophyUtils
                        .extendedBinomialCoefficient(completeGuestCountSource,
                                                     2);
            } else {
                final int completeGuestCountDestination =
                        state.getGuestCountAtHost(destination);

                totalCombinations = completeGuestCountSource
                        * completeGuestCountDestination;

            }

            return 1.0 / totalCombinations;
        }


    }

    public static abstract class DeathEvent extends CophylogeneticEvent {

        final protected NodeRef node;

        public DeathEvent(final String eventName,
                          final double eventHeight,
                          final NodeRef node) {
            super(eventName, eventHeight);
            this.node = node;
        }

        @Override
        public void apply(final MutableCophylogeneticTrajectoryState state)
                throws CophylogeneticEventFailedException {
            try {
                state.decrementGuestCountAtHost(node);
            } catch (CophylogeneticEventFailedException e) {
                throw new CophylogeneticEventFailedException(this);
            }
        }

    }

    public static class CophylogeneticEventFailedException
            extends RuntimeException {

        private static final long serialVersionUID = -1074026006660524662L;

        public CophylogeneticEventFailedException() {
            super("Event failed to make valid change to state");
        }

        public CophylogeneticEventFailedException(final
                                                  CophylogeneticEvent event) {
            super("Event " + event.getName()
                    + " failed to make valid change to state");
        }

    }

}
