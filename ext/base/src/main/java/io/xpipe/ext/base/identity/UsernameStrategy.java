package io.xpipe.ext.base.identity;

import io.xpipe.core.FailableSupplier;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Optional;

public interface UsernameStrategy {

    boolean hasUser();

    Optional<String> getFixedUsername();

    String retrieveUsername() throws Exception;

    @EqualsAndHashCode
    @ToString
    class None implements UsernameStrategy {

        @Override
        public boolean hasUser() {
            return false;
        }

        @Override
        public Optional<String> getFixedUsername() {
            return Optional.empty();
        }

        @Override
        public String retrieveUsername() {
            return null;
        }
    }

    @EqualsAndHashCode
    @ToString
    final class Fixed implements UsernameStrategy {

        private final String username;

        public Fixed(String username) {
            this.username = username;
        }

        public String get() {
            return username;
        }

        @Override
        public boolean hasUser() {
            return getFixedUsername().isPresent();
        }

        @Override
        public Optional<String> getFixedUsername() {
            return Optional.ofNullable(username);
        }

        @Override
        public String retrieveUsername() {
            return getFixedUsername().orElseThrow();
        }
    }

    final class Dynamic implements UsernameStrategy {

        private final FailableSupplier<String> username;

        public Dynamic(FailableSupplier<String> username) {
            this.username = username;
        }

        @Override
        public int hashCode() {
            return getClass().hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof Dynamic;
        }

        @Override
        public String toString() {
            return "<dynamic>";
        }

        @Override
        public boolean hasUser() {
            return true;
        }

        @Override
        public Optional<String> getFixedUsername() {
            return Optional.empty();
        }

        @Override
        public String retrieveUsername() throws Exception {
            var r = username.get();
            return r;
        }
    }
}
