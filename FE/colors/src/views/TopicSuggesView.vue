<template>
  <div>
    <div class="contentWindow">
      <div class="userInfo">
        {{ userName }}님 환영합니다!
        <i class="material-icons-outlined userIcon">account_circle</i>
      </div>
      <h2 class="topicTenTitle">토픽 제안 게시판</h2>
      <div class="selectbox">
        <select class="select" v-model="sortTitle" @change="onChange">
          <option disabled value="">정렬종류</option>
          <option value="regDate">등록일</option>
          <option value="recommand">추천수</option>
        </select>
        <select class="select" v-model="sort" @change="onChange">
          <option disabled value="">정렬방식</option>
          <option value="desc">내림차순</option>
          <option value="asc">오름차순</option>
        </select>
      </div>

      <topic-list class="topTenList" :isAdmin="isAdmin" :isTopic="true">
        <topic-article
          class="topicArticle"
          :isTopic="true"
          :isAdmin="isAdmin"
          v-for="topic in topics"
          :key="topic.id"
          :topicId="topic.id"
          :topicArticleTitle="topic.title"
          :recommend="topic.recommend"
          :cnt="topic.cnt"
          @clickLike="
            (data) => {
              topic.cnt = data.cnt;
              topic.recommend = data.recommend;
            }
          "
        />
      </topic-list>
      <div class="topTenBottomLine">
        <customButton btnText="돌아가기" @click="go" />
        <div class="pagenationContainer">
          <i class="material-symbols-rounded">keyboard_double_arrow_left</i>
          <i class="fa-solid fa-caret-left"></i>
          <customButton v-for="page in pages" :key="page" :btnText="page" @click="next(page)"></customButton>
          <i class="fa-solid fa-caret-right"></i>
          <i class="material-symbols-rounded">keyboard_double_arrow_right</i>
        </div>
        <customButton btnText="토픽 제안하기" @click="showModal = true" />
        <custom-modal class="suggestTopicModal" id="suggestTopicModal" v-show="showModal" @close-modal="showModal = false" titleText="Topic 제안">
          <cotent><suggest-modal></suggest-modal></cotent>
        </custom-modal>
      </div>
    </div>
  </div>
</template>

<script>
import TopicList from "@/components/topic/topicList.vue";
import TopicArticle from "@/components/topic/topicArticle.vue";
import SuggestModal from "@/components/topic/topicSuggest.vue";
import axios from "axios";

export default {
  components: { TopicList, TopicArticle, SuggestModal },
  data() {
    return {
      topics: null,
      currentPageNum: 0,
      sorting: "regDate,desc",
      maxPageNum: 0,
      showModal: false,
      userName: sessionStorage.getItem("userName"),
      pages: null,
      sortTitle: "regDate",
      sort: "desc",
      isAdmin: false,
    };
  },
  mounted() {
    let memberData = JSON.parse(sessionStorage.getItem("memberData"));
    var userId = memberData.data.id;
    axios
      .post(this.$store.state.baseurl + "topic/list?page=" + this.currentPageNum + "&sort=" + this.sorting, {
        userId: userId,
        keyword: "",
      })
      .then((response) => {
        if (!(response.data.message == "fail")) {
          console.log(response.data);
          this.maxPageNum = response.data.maxpage;
          this.topics = response.data.topics;
          var pageDecimical = parseInt(this.currentPage / 10);
          if (sessionStorage.getItem("auth") == "true") {
            this.isAdmin = true;
          }
          if (this.currentPage > 10) {
            // eslint-disable-next-line vue/no-mutating-props
            this.maxPageNum = this.maxPageNum % 10;
            // console.log(pageDecimical);
            this.pages = Array.from({ length: this.maxPageNum }, (item, index) => index + 1 + pageDecimical * 10 + "");
          } else {
            this.pages = Array.from({ length: this.maxPageNum }, (item, index) => index + 1 + "");
          }
        }
      });
  },
  methods: {
    onChange() {
      this.sorting = this.sortTitle + "," + this.sort;
      let memberData = JSON.parse(sessionStorage.getItem("memberData"));
      var userId = memberData.data.id;
      axios
        .post(this.$store.state.baseurl + "topic/list?page=" + this.currentPageNum + "&sort=" + this.sorting, {
          userId: userId,
          keyword: "",
        })
        .then((response) => {
          if (!(response.data.message == "fail")) {
            console.log(response.data);
            this.maxPageNum = response.data.maxpage;
            this.topics = response.data.topics;

            var pageDecimical = parseInt(this.currentPage / 10);
            if (this.currentPage > 10) {
              // eslint-disable-next-line vue/no-mutating-props
              this.maxPageNum = this.maxPageNum % 10;
              // console.log(pageDecimical);
              this.pages = Array.from({ length: this.maxPageNum }, (item, index) => index + 1 + pageDecimical * 10 + "");
            } else {
              this.pages = Array.from({ length: this.maxPageNum }, (item, index) => index + 1 + "");
            }
          }
        });
    },
    clikeLike(topic) {
      topic.cnt = topic.cnt + 1;
      topic.recommnd = !topic.recommnd;
    },
    clikeUnLike(topic) {
      topic.cnt = topic.cnt - 1;
      topic.recommnd = !topic.recommnd;
    },
    go() {
      this.$router.push("/enterPage");
    },
    next(page) {
      this.currentPageNum = page - 1;

      let memberData = JSON.parse(sessionStorage.getItem("memberData"));
      var userId = memberData.data.id;

      axios
        .post(this.$store.state.baseurl + "topic/list?page=" + this.currentPageNum + "&sort=" + this.sorting, {
          userId: userId,
          keyword: "",
        })
        .then((response) => {
          if (!(response.data.message == "fail")) {
            console.log(response.data);
            this.maxPageNum = response.data.maxpage;
            this.topics = response.data.topics;

            var pageDecimical = parseInt(this.currentPage / 10);
            if (this.currentPage > 10) {
              // eslint-disable-next-line vue/no-mutating-props
              this.maxPageNum = this.maxPageNum % 10;
              // console.log(pageDecimical);
              this.pages = Array.from({ length: this.maxPageNum }, (item, index) => index + 1 + pageDecimical * 10 + "");
            } else {
              this.pages = Array.from({ length: this.maxPageNum }, (item, index) => index + 1 + "");
            }
          }
        });
    },
  },
};
</script>

<style scoped>
.select {
  margin-left: 5px;
  border: 2px #d0d1ff solid;
  border-radius: 3px;
  width: 80px;
  height: 30px;
}
.selectbox {
  margin-left: 50px;
  margin-bottom: 10px;
  text-align: left;
}
.contentWindow {
  margin-left: 130px;
  min-height: 90vh;
}
.topTenList {
  padding: 0 3%;
}
.topicArticle {
  display: flex;
  justify-content: space-around;
  align-items: center;
  border-bottom: 1px solid #919aa9;
}
.userInfo {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  margin-right: 30px;
  margin-top: 20px;
  font: 20px "Pretendard Bold";
  color: #6667ab;
}
.userIcon {
  margin-left: 12px;
  font-size: 24px;
  font-weight: bold;
}
.topicTenTitle {
  font-family: "Pretendard ExtraBold";
  font-size: 36px;
  color: #6667ab;
  text-align: left;
  width: fit-content;
  margin-left: 60px;
  position: relative;
}
.topicTenTitle:after {
  content: "";
  width: 140%;
  display: block;
  position: absolute;
  /* margin-top: 50px; */
  border-bottom: 5px solid #d0d1ff;
}
.topTenBottomLine {
  display: flex;
  justify-content: space-between;
  margin: auto 5%;
  margin-top: 15px;
}
.topTenBottomLine button {
  font-family: "Pretendard ExtraBold";
  flex-basis: 15%;
}
.pagenationContainer {
  display: flex;
  align-items: center;
  gap: 3px;
}
.pagenationContainer i {
  font-size: 36px;
  color: #d0d1ff;
}
.pagenationContainer i:hover {
  color: #6667ab;
}
</style>
