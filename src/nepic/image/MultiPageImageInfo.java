package nepic.image;

import java.util.Iterator;

import nepic.util.Verify;

/**
 * 
 * @author AJ Parmidge
 * @since AutoCBFinder_Alpha_v0-9-2013-02-10
 * @version AutoCBFinder_Alpha_v0-9-2013-02-10
 */
public class MultiPageImageInfo implements Iterable<PageInfo> {
    private final PageInfo[] pages;

    public MultiPageImageInfo(int numPages) {
        // Initialize all pages so that all PageInfos are initially null
        pages = new PageInfo[numPages];
    }

    public int getNumPages() {
        return pages.length;
    }

    public void verifyPageNumLegal(int pgNum) {
        Verify.argument(pgNum > -1 && pgNum < pages.length,
                "The page number must be between 0 and " + (pages.length - 1) + " (inclusive)");
    }

    public PageInfo getPage(int pgNum) {
        verifyPageNumLegal(pgNum);
        return pages[pgNum];
    }

    public void setPage(PageInfo page) {
        Verify.notNull(page, "PageInfo to set cannot be null");
        int pgNum = page.getPageNum();
        verifyPageNumLegal(pgNum);
        pages[pgNum] = page;
    }

    @Override
    public Iterator<PageInfo> iterator() {
        return new Iterator<PageInfo>() {
            int pgNum = 0;

            @Override
            public boolean hasNext() {
                return pgNum < pages.length;
            }

            @Override
            public PageInfo next() {
                return pages[pgNum++];
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }

        };
    }

}
