package examples.optics

import io.estatico.newtype.macros.newtype
import monocle.Optional
import monocle.macros.GenLens
import monocle.std.option.some

object optionals extends App {

  @newtype case class AlbumName(value: String)
  case class Album(name: AlbumName, year: Int)
  case class Song(name: String, album: Option[Album])

  val albumNameLens = GenLens[Album](_.name)
  val songAlbumLens = GenLens[Song](_.album)

  val songAlbumNameOpt: Optional[Song, AlbumName] =
    songAlbumLens.composePrism(some).composeLens(albumNameLens)

  println("Optionals example")

  val album = Album(AlbumName("Peluso of Milk"), 1991)
  val song1 = Song("Ganges", Some(album))
  val song2 = Song("State of unconsciousness", None)

  println(songAlbumNameOpt.getOption(song1)) // Some(Peluso of Milk)
  println(songAlbumNameOpt.getOption(song2)) // None

}
