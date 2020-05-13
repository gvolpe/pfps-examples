let
  # unstable packages on April 24
  pkgs = import (fetchTarball "https://github.com/NixOS/nixpkgs-channels/archive/10100a97c89.tar.gz") {};
  stdenv = pkgs.stdenv;

in stdenv.mkDerivation rec {
  name = "pfps-examples";
  buildInputs = [
    pkgs.haskellPackages.dhall-json
    pkgs.openjdk
    pkgs.sbt
  ];
}
