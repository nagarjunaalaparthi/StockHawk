package com.stockhawk.model;

import android.net.Uri;

import net.simonvt.schematic.annotation.ContentProvider;
import net.simonvt.schematic.annotation.ContentUri;
import net.simonvt.schematic.annotation.InexactContentUri;
import net.simonvt.schematic.annotation.TableEndpoint;

@ContentProvider(authority = QuoteProvider.AUTHORITY, database = QuoteDatabase.class,packageName = "com.stockhawk.generated")
public class QuoteProvider {
  public static final String AUTHORITY = "com.stockhawk.QuoteProvider";

  static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

  interface Path{
    String QUOTES = "quotes";
  }

  private static Uri buildUri(String... paths){
    Uri.Builder builder = BASE_CONTENT_URI.buildUpon();
    for (String path:paths){
      builder.appendPath(path);
    }
    return builder.build();
  }

  @TableEndpoint(table = QuoteDatabase.QUOTES)
  public static class Quotes{
    @ContentUri(
        path = Path.QUOTES,
        type = "vnd.android.cursor.dir/quote"
    )
    public static final Uri CONTENT_URI = buildUri(Path.QUOTES);

    @InexactContentUri(
        name = "QUOTE_ID",
        path = Path.QUOTES + "/*",
        type = "vnd.android.cursor.item/quote",
        whereColumn = QuoteColumns.SYMBOL,
        pathSegment = 1
    )
    public static Uri withSymbol(String symbol){
      return buildUri(Path.QUOTES, symbol);
    }
  }
}
