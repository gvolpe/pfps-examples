package examples.optics

import io.estatico.newtype.macros.newtype
import monocle.macros.GenLens
import monocle.syntax.all._
import monocle.{Focus, Optional}

object optionals extends App {

  @newtype case class AlbumName(value: String)
  case class Album(name: AlbumName, year: Int)
  case class Song(name: String, album: Option[Album])

  val albumNameLens = GenLens[Album](_.name)
  val songAlbumLens = GenLens[Song](_.album)

  val songAlbumNameOpt: Optional[Song, AlbumName] =
    songAlbumLens.some.andThen(albumNameLens)

  val album = Album(AlbumName("Peluso of Milk"), 1991)
  val song1 = Song("Ganges", Some(album))
  val song2 = Song("State of unconsciousness", None)

  println("Optionals example using the new Focus API")

  def f(s: Song): Option[AlbumName] =
    s.focus(_.album).some.andThen(Focus[Album](_.name)).getOption

  println(f(song1))
  println(f(song2))

  println("Optionals example using the classic encoding")

  println(songAlbumNameOpt.getOption(song1)) // Some(Peluso of Milk)
  println(songAlbumNameOpt.getOption(song2)) // None

}
