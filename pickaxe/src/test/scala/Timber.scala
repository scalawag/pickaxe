import java.io.FileWriter
import org.scalawag.timber.backend.TimberConfiguration
import org.scalawag.timber.impl.formatter.DefaultEntryFormatter
import org.scalawag.timber.impl.receiver.{WriterReceiver,AutoFlush}

object Timber extends TimberConfiguration {
  override protected[this] lazy val formatter = new DefaultEntryFormatter(headerOnEachLine = true)
  override lazy val receiver = new WriterReceiver(new FileWriter("pickaxe/target/test.log"),formatter) with AutoFlush
}

/* pickaxe -- Copyright 2013 Justin Patterson -- All Rights Reserved */
