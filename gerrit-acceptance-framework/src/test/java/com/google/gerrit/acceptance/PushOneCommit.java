import org.eclipse.jgit.lib.ObjectId;
import java.util.List;

      .committer(new PersonIdent(i, testRepo.getClock()));
  }

  public void setParents(List<RevCommit> parents) throws Exception {
    commitBuilder.noParents();
    for (RevCommit p : parents) {
      commitBuilder.parent(p);
    }
  public Result execute(String ref) throws Exception {
    public ObjectId getCommitId() {
      return commit;
    }

        throws OrmException {
        throws OrmException {
      Iterable<Account.Id> actualIds =
          approvalsUtil.getReviewers(db, notesFactory.create(c)).values();