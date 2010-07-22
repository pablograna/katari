/* vim: set ts=2 et sw=2 cindent fo=qroca: */

package com.globant.katari.gadgetcontainer.domain;

import static java.util.UUID.randomUUID;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

import com.globant.katari.gadgetcontainer.SpringTestUtils;
import com.globant.katari.hibernate.coreuser.domain.CoreUser;
import com.globant.katari.shindig.domain.Application;

/**
 * Test for the repository {@link GadgetGroupRepository}
 *
 * @author waabox(emiliano[dot]arango[at]globant[dot]com)
 *
 */
public class GadgetGroupRepositoryTest {

  private static final String REPOSITORY
    = "gadgetcontainer.gadgetGroupRepository";
  private GadgetGroupRepository repository;
  private ApplicationContext appContext;
  private Session session;

  private CoreUser user;

  @Before
  public void setUp() throws Exception {
    appContext = SpringTestUtils.getContext();
    repository = (GadgetGroupRepository) appContext.getBean(REPOSITORY);
    user = new SampleUser("me");
    session = ((SessionFactory) appContext.getBean("katari.sessionFactory"))
        .openSession();
    session.createQuery("delete from GadgetInstance").executeUpdate();
    session.createQuery("delete from GadgetGroup").executeUpdate();
    session.createQuery("delete from CoreUser").executeUpdate();
    session.saveOrUpdate(user);
    user = (CoreUser) session.createQuery("from CoreUser").uniqueResult();
  }

  @After
  public void tearDown() {
    session.close();
  }

  /** This test, persist a new group, and the search it back in the db.
   *  check that the group has the same attributes.
   */
  @Test
  public void testFindPage() {
    String groupName = randomUUID().toString();
    String url = "http://" + randomUUID().toString();
    createGadgetGroup(user, groupName, url);

    GadgetGroup thePage = repository.findGadgetGroup(1, groupName);

    assertNotNull(thePage);
    assertFalse(thePage.getGadgets().isEmpty());
    assertTrue(groupName.equals(thePage.getName()));
    assertTrue(thePage.getGadgets().iterator().next().getUrl().equals(url));
  }

  /** This test, persist a new group, and the search it back in the db.
   *  check that the group has the same attributes.
   */
  @Test
  public void testFindPageNonExist() {
    GadgetGroup thePage = repository.findGadgetGroup(-1, "nonExist");
    assertNull(thePage);
  }

  /** Creates and persists a new group in the database.
   *
   * @param userId
   * @param groupName
   * @param gadgetUrl
   * @param gadgetPosition
   */
  private void createGadgetGroup(final CoreUser userId, final String groupName,
      final String gadgetUrl) {
    GadgetGroup group = new GadgetGroup(userId, groupName, 2);
    Application app = new Application(gadgetUrl);
    // Test friendly hack: never use the repository like this.
    repository.getHibernateTemplate().saveOrUpdate(app);
    group.addGadget(new GadgetInstance(app, 1, 2));
    repository.save(group);
  }
}
