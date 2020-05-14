let
  # unstable packages on May 13th
  pkgs = import (fetchTarball "https://github.com/NixOS/nixpkgs-channels/archive/6bcb1dec8ea.tar.gz") {};
  stdenv = pkgs.stdenv;

in stdenv.mkDerivation rec {
  name = "pfps-examples";
  buildInputs = [
    pkgs.haskellPackages.dhall-json
    pkgs.openjdk
    pkgs.sbt
  ];
}
