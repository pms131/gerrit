import com.google.gerrit.server.project.NoSuchChangeException;
      .committer(new PersonIdent(i, testRepo.getDate()));
  private Result execute(String ref) throws Exception {
  public void noParents() {
    commitBuilder.noParents();
  }

        throws OrmException, NoSuchChangeException {
        throws OrmException, NoSuchChangeException {
      Iterable<Account.Id> actualIds = approvalsUtil
          .getReviewers(db, notesFactory.createChecked(db, c))
          .values();