{ jdk ? "jdk15" }:

let
  config = {
    packageOverrides = p: rec {
      java = p.${jdk};

      sbt = p.sbt.overrideAttrs (
        old: rec {
          jre = "${java}";
        }
      );
    };
  };

  nixpkgs = fetchTarball {
    name   = "nixos-unstable-2021-02-21";
    url    = "https://github.com/NixOS/nixpkgs/archive/9816b99e71c.tar.gz";
    sha256 = "1dpz36i3vx0c1wmacrki0wsf30if8xq3bnj71g89rsbxyi87lhcm";
  };

  pkgs = import nixpkgs { inherit config; };
in
pkgs.mkShell {
  name = "scala-shell";

  buildInputs = [
    pkgs.coursier
    pkgs.${jdk}
    pkgs.sbt
  ];
}
