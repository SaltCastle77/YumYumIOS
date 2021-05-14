//
//  HomeViewController.swift
//  YumYum
//
//  Created by 염성훈 on 2021/04/17.
//

import UIKit
import GoogleSignIn
import SwiftyJSON
import Lottie

class HomeVC: UIViewController {
    
    static func instance() -> HomeVC {
        let vc = UIStoryboard.init(name: "Home", bundle: nil).instantiateViewController(withIdentifier: "HomeVC") as! HomeVC
        return vc
    }
    
    let cellIdentifier: String = "HomeCell"
    @IBOutlet var collectionView: UICollectionView!
    
    var feedList: [Feed] = []
    var myLikeFeedList: [Feed] = []

    
    let userData = UserDefaults.getLoginedUserInfo()!
        
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        collectionView.delegate = self
        collectionView.dataSource = self
        
        setLayout()
        
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        self.navigationController?.navigationBar.isHidden = true

        
        let userId = UserDefaults.getLoginedUserInfo()!["id"].intValue
        
        WebApiManager.shared.getFeedList(userId: userId) { (result) in
            if result["status"] == "200" {
                let results = result["data"]
                let list = results.arrayValue.compactMap({Feed(feedJson: $0)})
                self.feedList = list.filter { $0.isCompleted == true }
                self.collectionView.reloadData()
            }
        } failure: { (error) in
            print("에러에러")
            print("feed list error: \(error)")
        }
            
        WebApiManager.shared.getMyLikeFeed(userId: userId){ (result) in
            if result["status"] == "200" {
                let results = result["data"]
                self.myLikeFeedList = results.arrayValue.compactMap({Feed(feedJson: $0)})
            }
        } faliure: { (error) in
            print("내가 좋아요한 피드 리스트 에러 \(error)")
        }
        
        self.collectionView.reloadData()
    }
    
    
    func setLayout() {
        self.navigationController?.navigationBar.isHidden = true
        collectionView.isPagingEnabled = true
        let flowLayout: UICollectionViewFlowLayout = UICollectionViewFlowLayout()
        flowLayout.minimumLineSpacing = 0
        flowLayout.minimumInteritemSpacing = 0
        collectionView.collectionViewLayout = flowLayout
        collectionView.contentInsetAdjustmentBehavior = .never
    }
}


extension HomeVC:  UICollectionViewDelegate, UICollectionViewDataSource, UICollectionViewDelegateFlowLayout {
    // 해당 row에 이벤트가 발생했을때 출력되는 함수
    func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
        collectionView.deselectItem(at: indexPath, animated: true)
        print("You tapped me \(indexPath.row)")
    }
    
    func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        return self.feedList.count
    }
    
    
    // 컬렉션 뷰의 지정된 위치에 표시할 셀을 요청하는 메서드
    func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        
        let feedReverse = Array(feedList.reversed())
        let feed = feedReverse[indexPath.item]
        var myLikeFeed : Feed = Feed()
        
        for myfeed in myLikeFeedList {
            if feed.id == myfeed.id {
                myLikeFeed = myfeed
            }
        }
        
        let cell: VideoCollectionViewCell = collectionView.dequeueReusableCell(withReuseIdentifier: self.cellIdentifier, for: indexPath) as! VideoCollectionViewCell
        
        cell.index = indexPath.item
        cell.delegate = self
        cell.nowFeed = feed
        cell.setUpAnimation()
        cell.configureVideo(with: feed, myLikeFeed: myLikeFeed)
        
        return cell
    }
    
    @objc func goToUserProfile() {
        let storyboard: UIStoryboard? = UIStoryboard(name: "Home", bundle: nil)
        
        guard let peopleVC = storyboard?.instantiateViewController(withIdentifier: "PeopleVC") as? PeopleVC else {
            return
        }

        self.navigationController?.pushViewController(peopleVC, animated: true)
        
    }

    func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, sizeForItemAt indexPath: IndexPath) -> CGSize {
        let cvRect = self.collectionView.frame
        return CGSize(width: cvRect.width, height: cvRect.height)
    }
}

extension HomeVC:userProfileBtnDelegate {
    
    func userBtnPress(index: Int, nowfeed: Feed) {
        let storyboard: UIStoryboard? = UIStoryboard(name: "Home", bundle: nil)
        
        guard let peopleVC = storyboard?.instantiateViewController(withIdentifier: "PeopleVC") as? PeopleVC else {
            return
        }
        
        peopleVC.userId = nowfeed.user?.id!
        peopleVC.username = nowfeed.user?.nickname!
        
        WebApiManager.shared.getUserInfo(userId: (nowfeed.user?.id!)!) { (result) in
            if result["status"] == "200"{
                let results = result["data"]
                peopleVC.userData = User(json: results)
                self.navigationController?.pushViewController(peopleVC, animated: true)
            }
        } failure: { (error) in
            print(error)
        }
    }
}
